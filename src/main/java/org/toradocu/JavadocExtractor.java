package org.toradocu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.toradocu.doclet.formats.html.ConfigurationImpl;
import org.toradocu.doclet.formats.html.HtmlDocletWriter;
import org.toradocu.doclet.internal.toolkit.taglets.TagletWriter;
import org.toradocu.doclet.internal.toolkit.util.DocFinder;
import org.toradocu.doclet.internal.toolkit.util.DocFinder.Output;
import org.toradocu.doclet.internal.toolkit.util.DocPath;
import org.toradocu.doclet.internal.toolkit.util.ImplementedMethods;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.javadoc.Type;

public class JavadocExtractor {

	public static Set<JavadocExceptionComment> extract(ClassDoc classDoc, ConfigurationImpl configuration) throws IOException {
		Set<JavadocExceptionComment> extractedComments = new HashSet<>();
		TagletWriter tagletWriterInstance = new HtmlDocletWriter(configuration, DocPath.forClass(classDoc)).getTagletWriterInstance(false);   	
		
		// Loop on constructors and methods (also inherited) of the target class
		for (ExecutableMemberDoc member : getConstructorsAndMethods(classDoc)) {
			Output found = DocFinder.search(new DocFinder.Input(member));
			Doc holder = found.holder;
			List<Tag> tags = new ArrayList<>();
			
			Collections.addAll(tags, holder.tags("@throws"));
    		Collections.addAll(tags, holder.tags("@exception"));
    		
			// Collect information for methods defined in interfaces
			if (holder instanceof MethodDoc) {
				ImplementedMethods implementedMethods = new ImplementedMethods((MethodDoc) holder, configuration);
				for (MethodDoc implementedMethod : implementedMethods.build()) {
					Collections.addAll(tags, implementedMethod.tags("@throws"));
					Collections.addAll(tags, implementedMethod.tags("@exception"));
				}
			}
			
			// Note that we filter duplicated tags
    		for (Tag tag : filterDuplicates(tags)) {
    			ThrowsTag throwsTag = (ThrowsTag) tag;
        		String exception = getExceptionName(throwsTag);
    			String comment = tagletWriterInstance.commentTagsToOutput(tag, tag.inlineTags()).toString(); // Inline taglet such as {@inheritDoc}
    			comment = Jsoup.parse(comment).text(); //Remove HTML tags
    			
    			Doc method = tag.holder();
    			if (method instanceof ExecutableMemberDoc) {
    				/* Note that a JavadocExceptionComment refers to the method where the comment is defined.
    				   This means that if a comment is defined in an interface we refer to the interface's method. */
    				extractedComments.add(new JavadocExceptionComment(member, (ExecutableMemberDoc) method, exception, comment)); 
    			} else {
    				throw new AssertionError("This should never happen", null);
    			}
    		}
		}
		return extractedComments;
	}
	
	
	/**
	 * This method tries to return the qualified name of the exception in the <code>throwsTag</code>.
	 * If the source code of the exception is not available, type is null. Then
	 * we consider simply the name in the Javadoc comment.
	 * 
	 * @param throwsTag throw tag
	 * @return the exception name
	 */
	private static String getExceptionName(ThrowsTag throwsTag) {
		Type exceptionType = throwsTag.exceptionType();
		return exceptionType != null ? exceptionType.qualifiedTypeName() : throwsTag.exceptionName();
	}

	private static Set<Tag> filterDuplicates(List<Tag> tags) {
		Set<Tag> filteredTags = new HashSet<>();
		
		for (Tag tag : tags) {
			boolean unique = true;
			ThrowsTag tag_ = (ThrowsTag) tag;
			for (Tag filteredTag : filteredTags) {
				ThrowsTag filteredTag_ = (ThrowsTag) filteredTag;
				if (tag_.exceptionType().equals(filteredTag_.exceptionType()) 
						&& tag_.exceptionComment().equals(filteredTag_.exceptionComment())) {
					unique = false;
					break;
				}
			}
			if (unique) {
				filteredTags.add(tag_);
			}
		}
		return filteredTags;
	}

	/**
	 * @param classDoc a class
	 * @return the list of constructors and methods of <code>classDoc</code>
	 */
	private static Set<ExecutableMemberDoc> getConstructorsAndMethods(ClassDoc classDoc) {
		Set<ExecutableMemberDoc> membersToAnalyze = new HashSet<>();
		Set<String> membersAlreadyConsidered = new HashSet<>();
		ClassDoc currentClass = classDoc;
		
		do {
			List<ExecutableMemberDoc> currentMembers = new ArrayList<>();
			currentMembers.addAll(Arrays.asList(classDoc.constructors()));
			currentMembers.addAll(Arrays.asList(currentClass.methods()));
			// In this way we consider a method only once even when is overridden
			for (ExecutableMemberDoc member : currentMembers) {
				String memberID = member.name() + member.signature();
				if (!membersAlreadyConsidered.contains(memberID)) {
					membersToAnalyze.add(member);
					membersAlreadyConsidered.add(memberID);
				}
			}
			currentClass = currentClass.superclass();
		} while (currentClass != null && !currentClass.qualifiedName().equals("java.lang.Object"));
		return membersToAnalyze;
	}
}
