# Add project specific ProGuard rules here.
# Keep Room database entities
-keep class com.kiranaflow.app.data.model.** { *; }
# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
