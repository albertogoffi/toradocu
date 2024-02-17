package org.toradocu;

import static org.toradocu.translator.CommentTranslator.processCondition;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;
import org.toradocu.conf.Configuration;
import org.toradocu.extractor.DocumentedExecutable;
import org.toradocu.extractor.DocumentedType;
import org.toradocu.extractor.JavadocExtractor;
import org.toradocu.extractor.ParameterNotFoundException;
import org.toradocu.generator.OracleGenerator;
import org.toradocu.generator.TestGenerator;
import org.toradocu.generator.TestGeneratorValidation;
import org.toradocu.output.util.JsonOutput;
import org.toradocu.translator.CommentTranslator;
import org.toradocu.translator.semantic.SemanticMatcher;
import org.toradocu.util.GsonInstance;
import randoop.condition.specification.Guard;
import randoop.condition.specification.OperationSpecification;
import randoop.condition.specification.PostSpecification;
import randoop.condition.specification.PreSpecification;
import randoop.condition.specification.Property;
import randoop.condition.specification.ThrowsSpecification;

/**
 * Entry point of Toradocu. {@code Toradocu.main} is automatically executed
 * running the command: {@code java -jar toradocu.jar}.
 */
public class Toradocu {

	/** Command to run Toradocu. This string is used only in output messages. */
	private static final String TORADOCU_COMMAND = "java -jar toradocu.jar";
	/** Toradocu's configurations. */
	public static Configuration configuration = null;
	/** Logger of this class. */
	private static Logger log;

	/**
	 * Entry point for Toradocu. Takes several command-line arguments that configure
	 * its behavior.
	 *
	 * @param args command-line arguments
	 */
	public static void main(String[] args) {
		configuration = Configuration.INSTANCE;
		final JCommander jCommander = JCommander.newBuilder().addObject(configuration).programName(TORADOCU_COMMAND)
				.build();
		try {
			jCommander.parse(args);
		} catch (ParameterException e) {
			e.getJCommander().usage();
			System.out.println(e.getMessage());
			System.exit(1);
		}
		configuration.initialize();

		if (configuration.help()) {
			jCommander.usage();
			System.out.println("Options preceded by an asterisk are required.");
			System.exit(1);
		}

		if (configuration.debug()) {
			System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "trace");
		}

		// Suppress non-error messages from Stanford parser (some of the messages
		// directly printed on
		// standard error are still visible).
		System.setProperty(SimpleLogger.LOG_KEY_PREFIX + "edu.stanford", "error");
		log = LoggerFactory.getLogger(Toradocu.class);

		// === Javadoc Extractor ===

		List<DocumentedExecutable> members = null;
		final String targetClass = configuration.getTargetClass();
		if (configuration.getConditionTranslatorInput() == null) {
			final JavadocExtractor javadocExtractor = new JavadocExtractor();
			try {
				final DocumentedType documentedType = javadocExtractor.extract(targetClass,
						configuration.sourceDir.toString());
				members = documentedType.getDocumentedExecutables();
			} catch (ParameterNotFoundException e) {
				log.error(e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
				System.exit(1);
			} catch (ClassNotFoundException e) {
				log.error( // TODO Refine this error message for the specific caught exception.
						e.getMessage() + "\nPossible reasons for the error are:"
								+ "\n1. The Javadoc documentations is wrong"
								+ "\n2. The path to the source code of your system is wrong: " + configuration.sourceDir
								+ "\n3. The path to the binaries of your system is wrong: " + configuration.classDirs
								+ "\nPlease, check the correctness of the command line arguments."
								+ "\nIf the error persists, report the issue at "
								+ "https://github.com/albertogoffi/toradocu/issues" + "\nError stack trace:\n"
								+ Arrays.toString(e.getStackTrace()));
				System.exit(1);
			} catch (FileNotFoundException e) {
				e.printStackTrace(); // TODO Print a more meaningful message!
				System.exit(1);
			}
		}

		if (configuration.getJavadocExtractorOutput() != null) { // Print collection to the output file.
			try (BufferedWriter writer = Files.newBufferedWriter(configuration.getJavadocExtractorOutput().toPath(),
					StandardCharsets.UTF_8)) {
				writer.write(GsonInstance.gson().toJson(members));
			} catch (Exception e) {
				log.error("Unable to write the output on file "
						+ configuration.getJavadocExtractorOutput().getAbsolutePath(), e);
			}
		}
		if (configuration.debug()) {
			log.debug("Constructors/methods found in source code: " + members);
		}

		// === Condition Translator ===

		// Enable or disable semantic matching
		SemanticMatcher.setEnabled(configuration.isSemanticMatcherEnabled());

		if (configuration.isConditionTranslationEnabled()) {
			Map<DocumentedExecutable, OperationSpecification> specifications;

			// Use @tComment or the standard condition translator to translate comments.
			if (configuration.useTComment()) {
				specifications = null;// tcomment.TcommentKt.translate(members);
			} else {
				specifications = CommentTranslator.createSpecifications(members);
			}

			// Output the result on a file or on the standard output, if silent mode is
			// disabled.
			List<JsonOutput> jsonOutputs = new ArrayList<>();
			if (!configuration.isSilent() || !specifications.isEmpty()) {
				if (configuration.getConditionTranslatorOutput() != null) {
					try (BufferedWriter writer = Files.newBufferedWriter(
							configuration.getConditionTranslatorOutput().toPath(), StandardCharsets.UTF_8)) {

						for (DocumentedExecutable executable : specifications.keySet()) {
							jsonOutputs.add(new JsonOutput(executable, specifications.get(executable)));
						}
						String jsonOutput = GsonInstance.gson().toJson(jsonOutputs);
						writer.write(jsonOutput);
					} catch (Exception e) {
						log.error("Unable to write the output on file "
								+ configuration.getConditionTranslatorOutput().getAbsolutePath(), e);
					}
				} else {
					for (DocumentedExecutable member : members) {
						jsonOutputs.add(new JsonOutput(member, specifications.get(member)));
					}
					String jsonOutput = GsonInstance.gson().toJson(jsonOutputs);
					System.out.println("Condition translator output:\n" + jsonOutput);
				}
			}

			// Create statistics.
			/*
			 * File expectedResultFile = configuration.getExpectedOutput(); if
			 * (expectedResultFile != null) { Type collectionType = new
			 * TypeToken<List<JsonOutput>>() {}.getType(); try (BufferedReader reader =
			 * Files.newBufferedReader(expectedResultFile.toPath()); BufferedWriter
			 * resultsFile = Files.newBufferedWriter( configuration.getStatsFile().toPath(),
			 * StandardOpenOption.CREATE, StandardOpenOption.APPEND)) { List<JsonOutput>
			 * expectedResult = GsonInstance.gson().fromJson(reader, collectionType);
			 * List<Stats> targetClassResults = Stats.getStats(jsonOutputs, expectedResult);
			 * for (Stats result : targetClassResults) { if (result.numberOfConditions() !=
			 * 0) { // Ignore methods with no tags. resultsFile.write(result.asCSV());
			 * resultsFile.newLine(); } } } catch (IOException e) {
			 * log.error("Unable to read the file: " +
			 * configuration.getConditionTranslatorInput(), e); } }
			 */

			// Export generated specifications as Randoop specifications if requested.
			generateRandoopSpecs(specifications);

			// === Test Generator ===
			// Note that test generation is enabled only when translation is enabled.
			if (configuration.isTestGenerationEnabled()) {
				log.info("** Starting test generation...");
				try {
					TestGenerator.createTests(specifications);
					log.info("** Test generation completed");
				} catch (Throwable e) {
					e.printStackTrace();
					log.error("Error during test creation.", e);
				}
			} else {
				log.info("Test generator disabled: test generation skipped.");
			}

			// === Validation Test Generator ===
			if (configuration.isTestValidationEnabled()) {
				log.info("** Starting test generation for validation...");
				try {
					TestGeneratorValidation.createTests(specifications);
					log.info("** Test generation for validation completed");
				} catch (Throwable e) {
					e.printStackTrace();
					log.error("Error during validation test creation.", e);
				}
			} else {
				log.info("Validation test generator disabled: validation test generation skipped.");
			}

			// === Oracle Generator ===
			// Note that aspect generation is enabled only when translation is enabled.
			if (configuration.isOracleGenerationEnabled()) {
				try {
					OracleGenerator.createAspects(specifications);
				} catch (IOException e) {
					e.printStackTrace();
					log.error("Error during aspects creation.", e);
				}
			} else {
				log.info("Oracle generator disabled: aspect generation skipped.");
			}
		}
	}

	/**
	 * Export the specifications in {@code specsMap} to
	 * {@code conf.Configuration#randoopSpecsFile()} as Randoop specifications.
	 *
	 * @param specsMap the documented methods containing the specifications to
	 *                 export
	 */
	private static void generateRandoopSpecs(Map<DocumentedExecutable, OperationSpecification> specsMap) {
		File randoopSpecsFile = configuration.randoopSpecsFile();
		if (!configuration.isSilent() && randoopSpecsFile != null) {
			generateRandoopSpecsFile(randoopSpecsFile);
			Collection<OperationSpecification> randoopSpecs = new ArrayList<>();
			for (DocumentedExecutable documentedExecutable : specsMap.keySet()) {
				final OperationSpecification spec = specsMap.get(documentedExecutable);

				// Get rid of empty specifications.
				final List<PreSpecification> preSpecifications = spec.getPreSpecifications();
				preSpecifications.removeIf(s -> s.getGuard().getConditionText().isEmpty());
				final List<PostSpecification> postSpecifications = spec.getPostSpecifications();
				postSpecifications.removeIf(s -> s.getGuard().getConditionText().isEmpty());
				final List<ThrowsSpecification> throwsSpecifications = spec.getThrowsSpecifications();
				throwsSpecifications.removeIf(s -> s.getGuard().getConditionText().isEmpty());
				if (spec.isEmpty() || (preSpecifications.isEmpty() && postSpecifications.isEmpty()
						&& throwsSpecifications.isEmpty())) {
					continue;
				}

				// Convert specifications to Randoop format: args -> actual param name.
				final List<PreSpecification> randoopPreSpecs = convertPreSpecifications(documentedExecutable,
						preSpecifications);
				final List<PostSpecification> randoopPostSpecs = convertPostSpecifications(documentedExecutable,
						postSpecifications);
				final List<ThrowsSpecification> randoopThrowsSpecs = convertThrowsSpecifications(documentedExecutable,
						throwsSpecifications);

				final OperationSpecification newOperationSpec = new OperationSpecification(spec.getOperation(),
						spec.getIdentifiers(), randoopThrowsSpecs, randoopPostSpecs, randoopPreSpecs);
				randoopSpecs.add(newOperationSpec);
			}
			writeRandoopSpecsFile(randoopSpecsFile, randoopSpecs);
		}
	}

	private static List<PreSpecification> convertPreSpecifications(DocumentedExecutable documentedExecutable,
			List<PreSpecification> preSpecifications) {
		List<PreSpecification> newPreSpecifications = new ArrayList<>(preSpecifications.size());
		for (PreSpecification preSpecification : preSpecifications) {
			final Guard oldGuard = preSpecification.getGuard();
			Guard newGuard = new Guard(oldGuard.getDescription(),
					processCondition(oldGuard.getConditionText(), documentedExecutable));
			PreSpecification newSpec = new PreSpecification(preSpecification.getDescription(), newGuard);
			newPreSpecifications.add(newSpec);
		}
		return newPreSpecifications;
	}

	private static List<ThrowsSpecification> convertThrowsSpecifications(DocumentedExecutable documentedExecutable,
			List<ThrowsSpecification> throwsSpecifications) {
		List<ThrowsSpecification> newThrowsSpecifications = new ArrayList<>(throwsSpecifications.size());
		for (ThrowsSpecification throwsSpecification : throwsSpecifications) {
			final Guard oldGuard = throwsSpecification.getGuard();
			Guard newGuard = new Guard(oldGuard.getDescription(),
					processCondition(oldGuard.getConditionText(), documentedExecutable));
			ThrowsSpecification newSpec = new ThrowsSpecification(throwsSpecification.getDescription(), newGuard,
					throwsSpecification.getExceptionTypeName());
			newThrowsSpecifications.add(newSpec);
		}
		return newThrowsSpecifications;
	}

	private static List<PostSpecification> convertPostSpecifications(DocumentedExecutable documentedExecutable,
			List<PostSpecification> postSpecifications) {
		List<PostSpecification> newPostSpecifications = new ArrayList<>(postSpecifications.size());
		for (PostSpecification postSpec : postSpecifications) {
			final Guard oldGuard = postSpec.getGuard();
			Guard newGuard = new Guard(oldGuard.getDescription(),
					processCondition(oldGuard.getConditionText(), documentedExecutable));
			final Property oldProperty = postSpec.getProperty();
			Property newProperty = new Property(oldProperty.getDescription(),
					processCondition(oldProperty.getConditionText(), documentedExecutable));
			PostSpecification newSpec = new PostSpecification(postSpec.getDescription(), newGuard, newProperty);
			newPostSpecifications.add(newSpec);
		}
		return newPostSpecifications;
	}

	private static void writeRandoopSpecsFile(File randoopSpecsFile, Collection<OperationSpecification> specs) {
		try (BufferedWriter writer = Files.newBufferedWriter(randoopSpecsFile.toPath(), StandardOpenOption.WRITE,
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
			writer.write(GsonInstance.gson().toJson(specs));
		} catch (IOException e) {
			log.error("Error occurred during the export of generated specifications to file "
					+ randoopSpecsFile.getPath(), e);
		}
	}

	private static void generateRandoopSpecsFile(File randoopSpecsFile) {
		if (!randoopSpecsFile.exists()) {
			try {
				File parentDir = randoopSpecsFile.getParentFile();
				if (parentDir != null) {
					Files.createDirectories(parentDir.toPath());
				}

			} catch (IOException e) {
				log.error("Error occurred during creation of the file " + randoopSpecsFile.getPath(), e);
			}
		}
	}
}
