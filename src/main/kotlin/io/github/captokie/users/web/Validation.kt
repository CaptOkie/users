package io.github.captokie.users.web

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern
import kotlin.reflect.KClass

@NotBlank
@Pattern(regexp = "^\\S(?:[^\t\n\r\b]*\\S)?$")
@Constraint(validatedBy = [])
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.VALUE_PARAMETER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ValidString(val message: String = "{io.github.captokie.users.web.ValidString.message}", val groups: Array<KClass<*>> = [], val payload: Array<KClass<in Payload>> = [])
