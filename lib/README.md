Toradocu dependencies

Most dependencies are listed in `build.gradle` and are automatically
downloaded during the building process.  This direcory contains libraries
that are not available from repositories such as Maven Central.

* `lib/doclet.jar`: the custom doclet.  This doclet is essentially the same as
  the original doclet distributed with the JDK.  The only modified file is
  `org.toradocu.doclet.formats.html.HtmlDoclet` at line 204.
* `lib/tools-jdk1.8.0_72.jar`: the custom doclet depends on this jar that is
  part of the standard JDK distribution (original name: tools.jar).
