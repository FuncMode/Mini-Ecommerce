# Mini Ecommerce

## Overview

This is a simple e-commerce application built in Java, designed for basic online shopping functionality. It uses MySQL as the backend database to manage users, products, orders, and more. The project includes core features like user authentication, product catalog browsing, and order processing.

The codebase is structured with source files in the `src/` directory, compiled classes in `bin/`, and the MySQL JDBC driver in `lib/`.

## Features

- User registration and login
- Product listing and search
- Shopping cart management
- Order placement and tracking
- Admin panel for inventory (basic)

*(Note: Features are based on standard mini e-commerce implementations. Customize this section as needed once more details are available.)*

## Prerequisites

Before setting up the project, ensure you have the following installed:

- **Java Development Kit (JDK)**: Version 8 or higher
- **MySQL Server**: Version 8.0 or compatible (for the database)
- **Git**: For cloning the repository
- **Text Editor/IDE**: Such as VS Code, IntelliJ IDEA, or Eclipse (optional but recommended)

## Installation and Setup

### 1. Cloning the Repository

Clone the project from GitHub to your local machine:

```bash
git clone https://github.com/FuncMode/Mini-Ecommerce.git
cd Mini-Ecommerce
```

This will download all the source code, libraries, and any existing files.

### 2. Setting up the Database

1. Start your MySQL server.
2. Create a new database named `ecommerce_db` (or adjust as per your config):

   ```sql
   CREATE DATABASE ecommerce_db;
   ```

3. (Optional) If there are SQL scripts for tables and data, run them in your MySQL client (e.g., via phpMyAdmin or MySQL Workbench). Check the `sql/` directory if it exists, or create initial tables manually based on your models (e.g., users, products, orders).

### 3. Setting up .env File

The application uses a `.env` file for sensitive configuration, particularly database credentials. Create a file named `.env` in the root directory of the project:

```env
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/ecommerce_db?useSSL=false&serverTimezone=UTC
DB_USER=your_mysql_username
DB_PASSWORD=your_mysql_password

# Other optional configs (e.g., for app settings)
APP_PORT=8080
SECRET_KEY=your_jwt_secret_if_applicable
```

- Replace `your_mysql_username` and `your_mysql_password` with your actual MySQL credentials.
- Ensure the `.env` file is added to `.gitignore` to avoid committing sensitive info.

In your Java code, load these values using a library like `dotenv-java` (if integrated) or manually via `System.getenv()` or `Properties` file.

### 4. Verify Dependencies

- The MySQL JDBC driver (`mysql-connector-j-9.5.0.jar`) is already included in the `lib/` directory. No additional downloads needed.

## Building and Running the Application

### Compilation

Compile all Java source files from the `src/` directory. This creates bytecode in the `bin/` directory:

```bash
javac -d bin -cp "lib/mysql-connector-j-9.5.0.jar" src/*.java
```

- **Notes**:
  - On Windows, use `;` as the classpath separator.
  - On macOS/Linux, use `:` instead (e.g., `-cp "lib/mysql-connector-j-9.5.0.jar:bin"` for running).
  - If you have subpackages, adjust the `src/*.java` to include them (e.g., `src/**/*.java` with a more advanced build tool like Maven, but this is a basic setup).

### Running the Application

Execute the main class (`Main`) after compilation:

```bash
java -cp "bin;lib/mysql-connector-j-9.5.0.jar" Main
```

- **Notes**:
  - Again, use `;` for Windows and `:` for Unix-based systems.
  - The application should connect to the MySQL database using the `.env` config.
  - If successful, it will start the e-commerce server/console. Access it via browser (e.g., `http://localhost:8080`) if it's a web app, or interact via console.

## Usage

1. **Start the App**: Run the command above.
2. **Register/Login**: Use the provided endpoints or console prompts to create an account.
3. **Browse Products**: View the catalog and add items to cart.
4. **Place Orders**: Checkout with simulated payment.
5. **Admin Functions**: Log in as admin to manage inventory.

For detailed API endpoints or console commands, refer to the inline comments in the source code (e.g., `src/Main.java`).

## Troubleshooting

- **Compilation Errors**: Ensure JDK is installed and the classpath includes the JAR. Check for missing imports.
- **Database Connection Issues**: Verify MySQL is running, credentials in `.env` are correct, and the database exists.
- **ClassNotFoundException**: Confirm the `-cp` flag points to `bin` and `lib`.
- **Port Conflicts**: Change `APP_PORT` in `.env` if needed.

## Contributing

Feel free to fork the repo, make changes, and submit a pull request. Issues and suggestions are welcome!

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details (create one if not present).

---

*Happy coding! If you need to expand on any section (e.g., add screenshots or more features), let me know.*
