-injars 'builds/dist/libs/l2f-scripts-original.jar'
-outjars 'builds/dist/libs/l2f-scripts.jar'

-libraryjars 'builds/dist/libs/l2f-commons-original.jar'
-libraryjars 'builds/dist/libs/l2f-gameserver-original.jar'
-libraryjars <java.home>\lib\jce.jar
-libraryjars <java.home>\lib\rt.jar

# Use the obfuscation mapping of the related jars that were obfuscated aswell 
-applymapping 'builds/proguard/game-commons-mapping.map'

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

-keep class * {
    public <methods>;
}

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
