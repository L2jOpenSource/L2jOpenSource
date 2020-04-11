-injars builds\dist\libs\l2f-commons-original.jar
-outjars builds\dist\libs\l2f-commons.jar

-libraryjars <java.home>\lib\jce.jar
-libraryjars <java.home>\lib\rt.jar

# Save the obfuscation mapping to be used on the other obfuscations 
-printmapping 'builds/proguard/common-mapping.map'

-target 1.8
-overloadaggressively
-keepattributes Signature,InnerClasses,SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-adaptclassstrings **
-adaptresourcefilenames **.properties
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF
-dontshrink
-dontoptimize
-dontnote

# Keep all classes names that will be used with className dynamic constructors
-keep class **.commons.geometry.** { * ; }
-keep class **.commons.listener.** { * ; }

# Keep all classes that have AtomicIntegerFieldUpdater, cause in their initializer they use something special, and if the name is changed, it will fail
-keep class **.util.concurrent.** { * ; }

# Keep - Applications. Keep all application classes, along with their 'main'
# methods.
-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Also keep - Database drivers. Keep all implementations of java.sql.Driver.
-keep class * extends java.sql.Driver

# Also keep - Swing UI L&F. Keep all extensions of javax.swing.plaf.ComponentUI,
# along with the special 'createUI' method.
-keep class * extends javax.swing.plaf.ComponentUI {
    public static javax.swing.plaf.ComponentUI createUI(javax.swing.JComponent);
}
