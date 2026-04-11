Feature: Get Maintenance Request

  Background:
    * url 'http://localhost:8083'
    * def token = 'Bearer test-token'

  Scenario: Get a maintenance request by ID
    Given path '/api/v1/maintenance-requests/00000000-0000-0000-0000-000000000001'
    And header Authorization = token
    When method GET
    Then status 200
    And match response.id == '00000000-0000-0000-0000-000000000001'

  Scenario: Get a maintenance request with invalid ID returns 404
    Given path '/api/v1/maintenance-requests/00000000-0000-0000-0000-000000000999'
    And header Authorization = token
    When method GET
    Then status 404

  Scenario: Get maintenance request without token returns 401
    Given path '/api/v1/maintenance-requests/00000000-0000-0000-0000-000000000001'
    When method GET
    Then status 401