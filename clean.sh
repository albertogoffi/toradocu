#These commands eliminate comments between Javadoc and methods in some of
#our test sources, otherwise Javaparser won't recognize the Javadoc comment properly.
sed -i.bak '/@Pure/d' ./src/test/resources/src/plume-lib-1.1.0/java/src/plume/*.java
sed -i.bak '/@SideEffectFree/d' ./src/test/resources/src/plume-lib-1.1.0/java/src/plume/*.java
sed -i.bak '/@EnsuresQualifierIf/d' ./src/test/resources/src/plume-lib-1.1.0/java/src/plume/*.java
sed -i.bak '258d' ./src/test/resources/src/plume-lib-1.1.0/java/src/plume/RegexUtil.java

sed -i.bak '274,282d' ./src/test/resources/src/guava-19.0-sources/com/google/common/collect/ConcurrentHashMultiset.java

echo "Source cleaned! Unmodified files saved with .bak extension."
