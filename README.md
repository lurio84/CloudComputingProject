# Collaborative Notes Project

## General Description

Collaborative Notes is a Spring Boot-based application that allows users to create, edit, and share notes collaboratively in real-time. The application uses WebSockets to ensure smooth communication between users while they work simultaneously on their notes.

## Project Structure

The application is organized into several modules to ensure a clean and scalable architecture:

- **Backend**: Contains the business logic, controllers, and necessary configurations for the application.
    - **src/main/java/com/cloudComputing/collaborativeNotes**:
        - **config**: Configuration related to WebSockets and other settings.
        - **controllers**: REST controllers that handle client requests.
        - **services**: Business logic and data processing.
        - **repositories**: Interfaces to access the database using JPA.
        - **entities**: Classes that represent database tables.
        - **dtos**: Data Transfer Objects used for communication between layers.
        - **handlers**: Handlers for functionalities like WebSockets.
    - **src/main/resources**:
        - **application.properties**: Configuration for the database and other application parameters.

## Technologies Used

- **Java 17**: Main language of the project.
- **Spring Boot 3.3.5**: Framework for building the backend, simplifying configuration and deployment.
- **MariaDB**: Relational database to store notes and user data.
- **WebSockets**: To provide real-time collaborative editing capabilities.

## Project Configuration

1. **Database**:
    - The application is configured to use MariaDB. Make sure you have a MariaDB instance running and that the details in `application.properties` are correct.
    - Important variables in `application.properties`:
        - `spring.datasource.url`: Database URL (e.g., `jdbc:mariadb://localhost:3306/test`).
        - `spring.datasource.username` and `spring.datasource.password`: Credentials for database access. **(You should replace these with appropriate values or use environment variables)**.

2. **Running the Project**:
    - To run the application, you can use the following command:
      ```bash
      ./mvnw spring-boot:run
      ```
    - Alternatively, you can run the main class `CollaborativeNotesApplication` from IntelliJ.

## Current Endpoints

- **GET /**: Checks if the server is working correctly. Returns the message: `Server working properly`.

This is a basic endpoint to validate that the application is up and running.

## Next Steps

- **Develop Additional REST Endpoints**: Implement logic to create, update, and delete notes.
- **Implement Security**: Add user authentication and authorization.
- **API Documentation**: Consider using Swagger to document the API and facilitate the use of endpoints.

## Contributing

If you wish to contribute to this project, please follow these steps:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature/new-feature`).
3. Make your changes and commit them (`git commit -m 'Add new feature'`).
4. Submit a pull request for review.

## Contact

For questions or suggestions, you can contact the lead developer via the following email: [lucas.ronquillo@gmail.com](mailto:your-email@example.com).

