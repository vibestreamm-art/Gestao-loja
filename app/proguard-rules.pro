# Add project specific ProGuard rules here.
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes SourceFile,LineNumberTable

# Room entities and database
-keep class com.example.entities.** { *; }
-keep class com.example.database.** { *; }
-keep class com.example.domain.** { *; }
-dontwarn androidx.room.**
