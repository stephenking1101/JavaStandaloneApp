@Adding
Feature: Adding Test

  Scenario: Test Add
    Given I input "2" and "2"
    When the calculator is run
    Then the out put should be "4"

  Scenario Outline: test multi
    Given I input "<input1>" and "<input2>"
    When the calculator is run
    Then the out put should be "<result>"
    Examples:
      | input1 | input2 | result |
      | 2      | 3      | 5      |
      | 2      | 4      | 6      |
      | 3      | 3      | 6      |