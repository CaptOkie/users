package io.github.captokie.users.web

import javax.validation.Constraint
import javax.validation.Payload
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import kotlin.reflect.KClass

/**
 * A custom validation constraint for the commonly required [String] format. This applies the following constraints:
 *
 * - [Size] with a [Size.min] of 1 and a [Size.max] of 2
 * - [Pattern] with a [Pattern.regexp] of `^\S([^\t\n\r\b]\S)?$`
 */
@Size(min = 1, max = 100)
@Pattern(regexp = "^\\S(?:[^\t\n\r\b]*\\S)?$")
@Constraint(validatedBy = [])
@MustBeDocumented
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.FIELD, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.VALUE_PARAMETER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class StrictString(
        // These attributes are required by Hibernate.
        // See https://docs.jboss.org/hibernate/stable/validator/reference/en-US/html_single/#section-constraint-composition for more info
        val message: String = "{io.github.captokie.users.web.StrictString.message}",
        val groups: Array<KClass<*>> = [],
        val payload: Array<KClass<out Payload>> = []
)
