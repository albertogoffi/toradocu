#These commands eliminate comments between Javadoc and methods in some of
#our test sources, otherwise Javaparser won't recognize the Javadoc comment properly.
sed -i '/@Pure/d' ./src/test/resources/src/plume-lib-1.1.0/java/src/plume/*.java
sed -i '/@SideEffectFree/d' ./src/test/resources/src/plume-lib-1.1.0/java/src/plume/*.java
sed -i '/@EnsuresQualifierIf/d' ./src/test/resources/src/plume-lib-1.1.0/java/src/plume/*.java
sed -i '258d' ./src/test/resources/src/plume-lib-1.1.0/java/src/plume/RegexUtil.java

sed -i '274,282d' ./src/test/resources/src/guava-19.0-sources/com/google/common/collect/ConcurrentHashMultiset.java

echo "Source cleaned!"
