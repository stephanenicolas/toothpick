* Add tests if possible
* Run quality checks locally:
  ```bash
  ./gradlew clean check && ./gradlew -b toothpick-sample/build.gradle clean check
  ```
* Use `./gradlew spotlessApply` to format the code
* Just publish a normal PR
* Make the PR green
* Add the issue resolved and your name to the CHANGELOG.md
