@test
Feature: Parte Vehiculos

  Scenario: Consulta de un endpoint y comprobacion de un campo de la respuesta
    When Llamamos al a la API "vehiculosParte" con un metodo "POST" con el body "parteVehiculoFechaFutura"
    Then Se comprueba que devuelve un "400" y que el mensaje de error es "response_parteVehiculoFechaFutura"

