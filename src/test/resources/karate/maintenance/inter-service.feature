Feature: Inter-Service Communication

  Background:
    * url 'http://localhost:8083'
    * def token = 'Bearer test-token'

  Scenario: Get requests for manager calls Property Service successfully
    Given path '/api/v1/maintenance-requests'
    And header Authorization = token
    When method GET
    Then status 200

  Scenario: Assign staff validates against User Service
    Given path '/api/v1/maintenance-requests/00000000-0000-0000-0000-000000000001/assign'
    And header Authorization = token
    And request
      """
      {
        "staffId": "00000000-0000-0000-0000-000000000002"
      }
      """
    When method PUT
    Then status 200

  Scenario: Request without token returns 401
    Given path '/api/v1/maintenance-requests'
    When method GET
    Then status 401