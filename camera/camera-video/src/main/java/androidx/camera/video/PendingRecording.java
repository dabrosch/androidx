/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.camera.video;

import android.Manifest;
import android.content.Context;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.camera.core.impl.utils.ContextUtil;
import androidx.core.content.PermissionChecker;
import androidx.core.util.Consumer;
import androidx.core.util.Preconditions;

import java.util.concurrent.Executor;

/**
 * A recording that can be started at a future time.
 *
 * <p>A pending recording allows for configuration of a recording before it is started. Once a
 * pending recording is started with {@link #start(Executor, Consumer)}, any changes to the pending
 * recording will not affect the actual recording; any modifications to the recording will need
 * to occur through the controls of the {@link Recording} class returned by
 * {@link #start(Executor, Consumer)}.
 *
 * <p>A pending recording can be created using one of the {@link Recorder} methods for starting a
 * recording such as {@link Recorder#prepareRecording(Context, MediaStoreOutputOptions)}.

 * <p>There may be more settings that can only be changed per-recorder instead of per-recording,
 * because it requires expensive operations like reconfiguring the camera. For those settings, use
 * the {@link Recorder.Builder} methods to configure before creating the {@link Recorder}
 * instance, then create the pending recording with it.
 */
@RequiresApi(21) // TODO(b/200306659): Remove and replace with annotation on package-info.java
public final class PendingRecording {

    private final Context mContext;
    private final Recorder mRecorder;
    private final OutputOptions mOutputOptions;
    private Consumer<VideoRecordEvent> mEventListener;
    private Executor mListenerExecutor;
    private boolean mAudioEnabled = false;
    private boolean mIsPersistent = false;

    PendingRecording(@NonNull Context context, @NonNull Recorder recorder,
            @NonNull OutputOptions options) {
        // Application context is sufficient for all our needs, so store that to avoid leaking
        // unused resources. For attribution, ContextUtil.getApplicationContext() will retain the
        // attribution tag from the original context.
        mContext = ContextUtil.getApplicationContext(context);
        mRecorder = recorder;
        mOutputOptions = options;
    }

    /**
     * Returns an application context which was retrieved from the {@link Context} used to
     * create this object.
     */
    @NonNull
    Context getApplicationContext() {
        return mContext;
    }

    @NonNull
    Recorder getRecorder() {
        return mRecorder;
    }

    @NonNull
    OutputOptions getOutputOptions() {
        return mOutputOptions;
    }

    @Nullable
    Executor getListenerExecutor() {
        return mListenerExecutor;
    }

    @Nullable
    Consumer<VideoRecordEvent> getEventListener() {
        return mEventListener;
    }

    boolean isAudioEnabled() {
        return mAudioEnabled;
    }

    boolean isPersistent() {
        return mIsPersistent;
    }

    /**
     * Enables audio to be recorded for this recording.
     *
     * <p>This method must be called prior to {@link #start(Executor, Consumer)} to enable audio
     * in the recording. If this method is not called, the {@link Recording} generated by
     * {@link #start(Executor, Consumer)} will not contain audio, and
     * {@link AudioStats#getAudioState()} will always return
     * {@link AudioStats#AUDIO_STATE_DISABLED} for all {@link RecordingStats} send to the listener
     * set passed to {@link #start(Executor, Consumer)}.
     *
     * <p>Recording with audio requires the {@link android.Manifest.permission#RECORD_AUDIO}
     * permission; without it, recording will fail at {@link #start(Executor, Consumer)} with an
     * {@link IllegalStateException}.
     *
     * @return this pending recording
     * @throws IllegalStateException if the {@link Recorder} this recording is associated to
     * doesn't support audio.
     * @throws SecurityException if the {@link Manifest.permission#RECORD_AUDIO} permission
     * is denied for the current application.
     */
    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    @NonNull
    public PendingRecording withAudioEnabled() {
        // Check permissions and throw a security exception if RECORD_AUDIO is not granted.
        if (PermissionChecker.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                == PermissionChecker.PERMISSION_DENIED) {
            throw new SecurityException("Attempted to enable audio for recording but application "
                    + "does not have RECORD_AUDIO permission granted.");
        }
        Preconditions.checkState(mRecorder.isAudioSupported(), "The Recorder this recording is "
                + "associated to doesn't support audio.");
        mAudioEnabled = true;
        return this;
    }

    /**
     * Configures the recording to be a persistent recording.
     *
     * <p>A persistent recording will only be stopped by explicitly calling
     * {@link Recording#stop()} or {@link Recording#close()} and will ignore events that would
     * normally cause recording to stop, such as lifecycle events or explicit unbinding of a
     * {@link VideoCapture} use case that the recording's {@link Recorder} is attached to.
     *
     * <p>A {@link Recorder} instance is recommended to be associated with a single
     * {@link VideoCapture} instance, especially when using persistent recording. Otherwise, there
     * might be unexpected behavior. Any in-progress persistent recording created from the same
     * {@link Recorder} should be stopped before starting a new recording, even if the
     * {@link Recorder} is associated with a different {@link VideoCapture}.
     */
    @RestrictTo(Scope.LIBRARY_GROUP)
    @NonNull
    public PendingRecording asPersistentRecording() {
        mIsPersistent = true;
        return this;
    }

    /**
     * Starts the recording, making it an active recording.
     *
     * <p>Only a single recording can be active at a time, so if another recording is active,
     * this will throw an {@link IllegalStateException}.
     *
     * <p>If there are no errors starting the recording, the returned {@link Recording}
     * can be used to {@link Recording#pause() pause}, {@link Recording#resume() resume},
     * or {@link Recording#stop() stop} the recording.
     *
     * <p>Upon successfully starting the recording, a {@link VideoRecordEvent.Start} event will
     * be the first event sent to the provided event listener.
     *
     * <p>If errors occur while starting the recording, a {@link VideoRecordEvent.Finalize} event
     * will be the first event sent to the provided listener, and information about the error can
     * be found in that event's {@link VideoRecordEvent.Finalize#getError()} method. The returned
     * {@link Recording} will be in a finalized state, and all controls will be no-ops.
     *
     * <p>If the returned {@link Recording} is garbage collected, the recording will be
     * automatically stopped. A reference to the active recording must be maintained as long as
     * the recording needs to be active. If the recording is garbage collected, the
     * {@link VideoRecordEvent.Finalize} event will contain error
     * {@link VideoRecordEvent.Finalize#ERROR_RECORDING_GARBAGE_COLLECTED}.
     *
     * @throws IllegalStateException if the associated Recorder currently has an unfinished
     * active recording.
     * @param listenerExecutor the executor that the event listener will be run on.
     * @param listener the event listener to handle video record events.
     */
    @NonNull
    @CheckResult
    public Recording start(
            @NonNull Executor listenerExecutor,
            @NonNull Consumer<VideoRecordEvent> listener) {
        Preconditions.checkNotNull(listenerExecutor, "Listener Executor can't be null.");
        Preconditions.checkNotNull(listener, "Event listener can't be null");
        mListenerExecutor = listenerExecutor;
        mEventListener = listener;
        return mRecorder.start(this);
    }
}
