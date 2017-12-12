package example;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
//If runner is not linked to any feature file (no features section defined in @CucumberOptions, like this one) then runner automatically searches for all feature files in current and all child folders.
@CucumberOptions(
		plugin = {
                "json:target/cucumber/wikipedia.json",
                "html:target/cucumber/wikipedia.html",
                "pretty"
        },
        tags = {"~@ignored"}
)
//Runner above runs all feature files which does not have tag @ignored and outputs the results in three different formats: JSON in target/cucumber/wikipedia.json file, HTML in target/cucumber/wikipedia.html and Pretty – a console output with colours.
public class RunCukesTest {
}