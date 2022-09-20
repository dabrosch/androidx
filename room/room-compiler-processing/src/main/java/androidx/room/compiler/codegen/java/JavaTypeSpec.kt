/*
 * Copyright 2022 The Android Open Source Project
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

package androidx.room.compiler.codegen.java

import androidx.room.compiler.codegen.NONNULL_ANNOTATION
import androidx.room.compiler.codegen.NULLABLE_ANNOTATION
import androidx.room.compiler.codegen.VisibilityModifier
import androidx.room.compiler.codegen.XAnnotationSpec
import androidx.room.compiler.codegen.XCodeBlock
import androidx.room.compiler.codegen.XFunSpec
import androidx.room.compiler.codegen.XTypeSpec
import androidx.room.compiler.processing.XNullability
import com.squareup.javapoet.FieldSpec
import com.squareup.kotlinpoet.javapoet.JClassName
import com.squareup.kotlinpoet.javapoet.JTypeName
import javax.lang.model.element.Modifier

internal class JavaTypeSpec(
    override val className: JClassName,
    internal val actual: com.squareup.javapoet.TypeSpec
) : JavaLang(), XTypeSpec {

    internal class Builder(
        private val className: JClassName,
        internal val actual: com.squareup.javapoet.TypeSpec.Builder
    ) : JavaLang(), XTypeSpec.Builder {
        override fun superclass(typeName: JTypeName) = apply {
            actual.superclass(typeName)
        }

        override fun addAnnotation(annotation: XAnnotationSpec) {
            require(annotation is JavaAnnotationSpec)
            actual.addAnnotation(annotation.actual)
        }

        override fun addProperty(
            typeName: JTypeName,
            name: String,
            nullability: XNullability,
            visibility: VisibilityModifier,
            isMutable: Boolean,
            initExpr: XCodeBlock?,
            annotations: List<XAnnotationSpec>
        ) = apply {
            actual.addField(
                FieldSpec.builder(typeName, name).apply {
                    val visibilityModifier = visibility.toJavaVisibilityModifier()
                    // TODO(b/247242374) Add nullability annotations for non-private fields
                    if (visibilityModifier != Modifier.PRIVATE) {
                        if (nullability == XNullability.NULLABLE) {
                            addAnnotation(NULLABLE_ANNOTATION)
                        } else if (nullability == XNullability.NONNULL) {
                            addAnnotation(NONNULL_ANNOTATION)
                        }
                    }
                    addModifiers(visibilityModifier)
                    if (!isMutable) {
                        addModifiers(Modifier.FINAL)
                    }
                    initExpr?.let {
                        require(it is JavaCodeBlock)
                        initializer(it.actual)
                    }
                    // TODO(b/247247439): Add other annotations
                }.build()
            )
        }

        override fun addFunction(functionSpec: XFunSpec) = apply {
            require(functionSpec is JavaFunSpec)
            actual.addMethod(functionSpec.actual)
        }

        override fun build(): XTypeSpec {
            return JavaTypeSpec(className, actual.build())
        }
    }
}