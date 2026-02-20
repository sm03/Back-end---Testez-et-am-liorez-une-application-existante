# TESTING

Les tests sont réalisés avec l'extension de Spring et Mockito.

## Pyramide de tests

### Tests E2E

### Tests d'Intégration

### Tests Unitaires

Les tests fournis dans le projet initial concernent les classes UserServiceTest et UserControllerTest.

**UserServiceTest**

Les tests initiaux ont pour objet la méthode register:
- test_create_null_user_throws_IllegalArgumentException
- test_create_already_exist_user_throws_IllegalArgumentException
- test_create_user

Le test suivant a été ajouté pour la méthode register:
- register_shouldEncodePasswordBeforeSaving

Ces tests ont pour objet de tester ce qui suit:
- register OK
- login dupliqué
- utilisateur null
- chiffrage du password + vérification de la sauvegarde

Les tests suivants ont été ajoutés pour tester la méthode login:
- login_shouldReturnJwt_whenCredentialsAreValid
- login_shouldThrowException_whenPasswordInvalid
- login_shouldThrowException_whenUserNotFound
- login_shouldThrowException_whenLoginNull
- login_shouldThrowException_whenPasswordNull

Ces tests ont pour objet de tester ce qui suit:
- login OK → token retourné
- mauvais mot de passe → exception
- user inexistant → exception
- login null → exception
- password null → exception

La couverture de test pour UserService passe alors à 100%.

**UserControllerTest**

UserControllerTest utilise un mock Docker pour la BDD MySQL (MySQLContainer).
La version de MySQL du code initial était la dernière disponible ("latest").
En 2026, cela correspond à la version 9, incompatible avec Testcontainers.
La version a été fixée à 8.0 afin de rétablir la fonctionalité.
MySQLContainer n'est cependant pas utilisé dans les tests initialement fournis...

Ces tests reposent sur des classes non mockées (UserService...) hormis MockMvc.
Les tests ajoutés pour tester la méthodue suivent forcément le même principe qui nuit à l'isolation des tests puisqu'un appel à la méthode register au préalable est nécessaire.

Les tests initiaux ont pour objet la méthode register:
- registerUserWithoutRequiredData
- registerAlreadyExistUser
- registerUserSuccessful

Les tests suivants ont été ajoutés pour tester la méthode login:
- login_shouldReturnJwtToken_whenCredentialsValid
- login_shouldReturnUnauthorized_whenInvalidCredentials

La couverture de test pour UserController passe alors à 100%.

**StudentServiceImplTest**

Tests implémentés sur un Mock StudentRepository et un InjectMocks StudentServiceImpl:
- createStudent_shouldSaveAndReturnDTO
- getAllStudents_shouldReturnList
- getAllStudents_shouldReturnEmptyList
- getStudentById_shouldReturnStudent
- getStudentById_shouldThrow_whenNotFound
- updateStudent_shouldUpdateAndReturnDTO
- updateStudent_shouldThrow_whenNotFound
- deleteStudent_shouldDelete_whenExists
- deleteStudent_shouldThrow_whenNotFound
Un constructeur supplémentaire pour Student avec ID a été créé pour les tests car c'est un membre généré par la BDD (autoincrémenté).

La couverture de test pour StudentServiceImpl est de 100%.

**StudentControllerTest**

Tests implémentés sur un MockMvc et de vrais objets pour vérification effective.
Un User est créé avant tout (bibliothécaire) qui se loggue avant chaque test afin d'obtenir un token valable.
- createStudent_shouldReturnCreatedStudent
- createStudent_withoutToken_shouldBeUnauthorized
- getAllStudents_shouldReturnList
- getStudent_shouldReturnStudent
- getStudent_shouldReturn500_whenNotFound
- updateStudent_shouldReturnUpdatedStudent
- deleteStudent_shouldReturnNoContent
- deleteStudent_shouldReturn500_whenNotFound

La couverture de test pour StudentController est de 100%.


