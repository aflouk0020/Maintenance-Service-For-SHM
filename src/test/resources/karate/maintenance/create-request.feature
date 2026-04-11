Feature: Create Maintenance Request

  Background:
    * url 'http://localhost:8083'
    * def token = 'Bearer test-token'

  Scenario: Create a maintenance request successfully
    Given path '/api/v1/maintenance-requests'
    And header Authorization = token
    And request
      """
      {
        "propertyId": "00000000-0000-0000-0000-000000000001",
        "createdByUserId": "00000000-0000-0000-0000-000000000002",
        "description": "Leaking pipe in kitchen",
        "priority": "HIGH"
      }
      """
    When method POST
    Then status 201
    And match response.status == 'OPEN'
    And match response.priority == 'HIGH'

  Scenario: Create request with missing fields returns 400
    Given path '/api/v1/maintenance-requests'
    And header Authorization = token
    And request {}
    When method POST
    Then status 400