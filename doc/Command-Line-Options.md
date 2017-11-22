# Toradocu Command Line Options
Options marked with an asterisk (`*`) are mandatory.

## General Options
| Option | Description |
| :--- | --- |
| `--target-class` * | Fully-qualified name of the class for which Toradocu has to generate test oracles. |
| `--source-dir` * | Directory containing source files of the system under test (the system that includes the target class). |
| `--class-dir` * | Jars or directories containing binary files of the system under test (the system that includes the target class) and its dependencies. Use the standard classpath separator to provide more than one path. |
| `--stats-file` | File path where to save Toradocu statistics in CSV format. |
| `--silent` | Do not produce any output if there is no translated comment. |
| `--help` `-h` | Print the list of available options. |
| `--debug` | Enable fine-grained logging. |

## Javadoc Extractor Options
| Option | Description |
| :--- | --- |
| `--javadoc-extractor-output` | File path where to save the Javadoc extractor output in JSON format. |

## Condition Translator Options
| Option | Description |
| :--- | --- |
| `--condition-translation` | [`true/false`] Enable/disable the translation of the Javadoc comments. Default value: true. |
| `--distance-threshold` | Only code elements with edit distance less than this threshold will be considered candidates for translation. Must be a positive integer number. Default value: 2. |
| `--word-removal-cost` | Cost of a single word deletion in the edit distance algorithm. Must be a positive integer number. Default value: 1. |
| `--disable-semantics` | [`true/false`] Disable/enable the semantic-based translator. Default value: false (semantic-based translator enabled). |
| `--remove-commas` | Remove commas before a Javadoc comment text is parsed. Default value: true. |
| `--condition-translator-input` | File path to JSON file to be read as input of the condition translator. This option disables the Javadoc extractor. |
| `--condition-translator-output` | File path where to save the condition translator output in JSON format. If not provided the result of the condition translation phase is printed on the standard output. |
| `--expected-output` | Condition translator goal output file (in JSON format) used to compute Toradocu precision and recall. |
| `--tcomment` | Instead of the standard Toradocu's condition translator, use @tComment as translation algorithm for translating the Javadoc comments. |
| `--randoop-specs` | Export to the specified file path the generated specifications as JSON Randoop input specifications. (NOOP if --silent is specified.) |

## Oracle Generator Options
| Option | Description |
| :--- | --- |
| `--oracle-generation` | [`true/false`] Enable/disable the generation of the aspectJ aspects. Default value: true. |
| `--test-class` | Fully-qualified name of the class (your test suite) that will be instrumented with aspects. If the oracle generator is enabled, you have to provide a valid value for this option. |
| `--aspects-output-dir` | Directory path where to save the generated aspects. Default value: aspects. |
