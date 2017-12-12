package example.testcase;

import static org.junit.Assert.assertEquals;
import cucumber.api.CucumberOptions;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
//import cucumber.api.testng.AbstractTestNGCucumberTests;

@CucumberOptions(tags="@Adding")
public class AddingTest// extends AbstractTestNGCucumberTests //this is to run the cucumber with TestNG instead of junit
{
	private int input1;
    private int input2;
    private int result;

    @Given("^I input \"([^\"]*)\" and \"([^\"]*)\"$")
    public void iInputAnd(int input1, int input2) throws Throwable {
        this.input1 = input1;
        this.input2 = input2;
    }

    @When("^the calculator is run$")
    public void theCalculatorIsRun() throws Throwable {
        result = input1 + input2;
    }

    @Then("^the out put should be \"([^\"]*)\"$")
    public void theOutPutShouldBe(int expectResult) throws Throwable {
        assertEquals(expectResult, result);
    }
}
