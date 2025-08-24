# Java-GIS-enterprise-Application-with-AI-and-AWS
The JavaGIS Enterprise Application represents a comprehensive, enterprise-grade geospatial intelligence platform specifically designed for environmental monitoring and asset tracking requirements. This full-stack application combines cutting-edge web technologies with specialized geospatial capabilities to deliver a professional solution.


## Prerequisites

Before setting up the project, ensure you have the following installed:

- **Java Development Kit (JDK)** - Version 11 or higher
- **IntelliJ IDEA** - Ultimate Edition recommended for full Spring Boot support
- **PostgreSQL** - Version 12 or higher with PostGIS extension
- **Maven** - Version 3.6 or higher
- **Git** - For version control
- **AWS Account** - For cloud functionality (optional for local development)

## Project Setup in IntelliJ

1. **Clone the Repository**:
   - Open IntelliJ IDEA
   - Select `File > New > Project from Version Control`
   - Enter the repository URL and click `Clone`

2. **Import as Maven Project**:
   - When prompted, select `Import as Maven Project`
   - Wait for Maven to download all dependencies

3. **Configure JDK**:
   - Go to `File > Project Structure > Project`
   - Set the Project SDK to JDK 11 or higher
   - Set the Project language level to match your JDK

4. **Install Required Plugins**:
   - Go to `File > Settings > Plugins`
   - Install the following plugins if not already installed:
     - Spring Boot
     - Database Tools and SQL
     - AWS Toolkit
     - Lombok

5. **Enable Annotation Processing**:
   - Go to `File > Settings > Build, Execution, Deployment > Compiler > Annotation Processors`
   - Check `Enable annotation processing`

## Database Configuration

1. **Install PostgreSQL with PostGIS**:
   - Download and install PostgreSQL from [postgresql.org](https://www.postgresql.org/download/)
   - Install the PostGIS extension using Stack Builder or direct download

2. **Create Database**:
   ```sql
   CREATE DATABASE user_javagis;
   ```

3. **Enable PostGIS Extension**:
   ```sql
   \c user_javagis
   CREATE EXTENSION postgis;
   ```

4. **Configure Database Connection in IntelliJ**:
   - Go to `View > Tool Windows > Database`
   - Click the `+` icon and select `Data Source > PostgreSQL`
   - Enter your database credentials:
     - Host: localhost
     - Port: 5432
     - Database: user_javagis
     - User: your_username
     - Password: your_password
   - Test the connection and click `Apply`

5. **Update Application Properties**:
   - Open `src/main/resources/application.yml`
   - Update the database configuration:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/_javagis
       username: your_username
       password: your_password
   ```

## Running the Application

1. **Build the Project**:
   - Go to `View > Tool Windows > Maven`
   - Run `clean` and then `install`

2. **Run the Application**:
   - Navigate to `src/main/java/com/..../javagis/JavaGisApplication.java`
   - Right-click and select `Run 'JavaGisApplication'`
   - Alternatively, use the run configuration in the top toolbar

3. **Verify the Application is Running**:
   - Open a web browser and navigate to `http://localhost:8080/swagger-ui.html`
   - You should see the Swagger UI with all available API endpoints

4. **Default Credentials**:
   - The application comes with a default admin user:
     - Username: admin
     - Password: admin123
   - Use these credentials to authenticate and obtain a JWT token

## Testing the Application

1. **Run Unit Tests**:
   - Go to `View > Tool Windows > Maven`
   - Run `test`

2. **Run Integration Tests**:
   - Go to `View > Tool Windows > Maven`
   - Run `verify`

3. **Manual Testing with Swagger UI**:
   - Navigate to `http://localhost:8080/swagger-ui.html`
   - Authenticate using the `/auth/login` endpoint
   - Use the returned JWT token in the `Authorize` button at the top
   - Test various endpoints

4. **Validation Profile**:
   - The application includes a validation profile that tests all components
   - To run with validation:
     - Edit the run configuration
     - Add `--spring.profiles.active=validation` to VM options
     - Run the application

## AWS Configuration

1. **AWS Credentials Setup**:
   - Create an AWS IAM user with appropriate permissions
   - Configure AWS credentials in one of the following ways:
     - AWS credentials file (`~/.aws/credentials`)
     - Environment variables
     - Application properties

2. **Update AWS Configuration in Application Properties**:
   - Open `src/main/resources/application.yml`
   - Update the AWS configuration:
   ```yaml
   aws:
     region: eu-central-1  # Frankfurt region ( headquarters region)
     credentials:
       access-key: your_access_key
       secret-key: your_secret_key
     s3:
       bucket: your-user-javagis-bucket
   ```

3. **Create Required AWS Resources**:
   - S3 bucket for storing geospatial assets
   - Lambda functions for serverless processing
   - CloudWatch for monitoring

4. **AWS Toolkit Integration**:
   - Go to `View > Tool Windows > AWS Explorer`
   - Connect using your AWS credentials
   - Browse and manage AWS resources directly from IntelliJ

## Key Features Overview

The application includes the following key features:

1. **Geospatial Data Management**:
   - Upload, view, update, and delete satellite images
   - Upload, view, update, and delete shapefiles
   - Perform spatial queries and analysis

2. **STAC Catalog**:
   - Organize geospatial assets using STAC (Spatio-Temporal Asset Catalog)
   - Search and filter assets by temporal and spatial parameters
   - Manage collections and items

3. **AI Workflow Automation**:
   - Create and manage AI models for environmental monitoring
   - Set up automated workflows for image analysis
   - Schedule recurring tasks with cron expressions

4. **AWS Integration**:
   - Store assets in S3
   - Process data with Lambda functions
   - Monitor application with CloudWatch

5. **Security**:
   - JWT-based authentication
   - Role-based access control
   - Fine-grained permissions

## Extending the Application

1. **Adding New Entity Types**:
   - Create new domain classes in `com.user.javagis.domain`
   - Create corresponding repositories in `com.user.javagis.repository`
   - Create DTOs in `com.user.javagis.dto`
   - Create services in `com.user.javagis.service`
   - Create controllers in `com.user.javagis.controller`

2. **Adding New AI Models**:
   - Implement new AI model types in `com.user.javagis.domain.ai`
   - Update the AI service to support the new model types
   - Create appropriate endpoints in the controller

3. **Adding New AWS Integrations**:
   - Add new AWS service clients in `com.user.javagis.config.AwsConfig`
   - Create service classes in `com.user.javagis.service.aws`
   - Create controller endpoints as needed

4. **Customizing for -Specific Use Cases**:
   - Modify the STAC schema to match specific  data requirements
   - Customize AI workflows for specific environmental monitoring needs
   - Add facility-specific metadata and tracking

## Deployment Guide

1. **Packaging the Application**:
   - Go to `View > Tool Windows > Maven`
   - Run `package`
   - The resulting JAR file will be in the `target` directory

2. **Deployment Options**:
   - **AWS Elastic Beanstalk**:
     - Create a new Elastic Beanstalk environment
     - Upload the JAR file
     - Configure environment properties

   - **AWS EC2**:
     - Launch an EC2 instance
     - Install Java and PostgreSQL with PostGIS
     - Upload and run the JAR file

   - **Docker**:
     - Build the Docker image using the provided Dockerfile
     - Push to Amazon ECR or another container registry
     - Deploy to ECS, EKS, or Fargate

3. **Database Migration**:
   - The application uses Flyway for database migrations
   - Migrations are automatically applied on startup
   - Custom migrations can be added in `src/main/resources/db/migration`

## Troubleshooting

1. **Database Connection Issues**:
   - Verify PostgreSQL is running
   - Check database credentials
   - Ensure PostGIS extension is installed

2. **AWS Connectivity Issues**:
   - Verify AWS credentials
   - Check IAM permissions
   - Ensure network connectivity to AWS services

3. **Application Startup Issues**:
   - Check application logs in the IntelliJ console
   - Verify all required dependencies are resolved
   - Check for port conflicts

4. **Performance Issues**:
   - Monitor application metrics in CloudWatch
   - Check database query performance
   - Optimize large file uploads and processing

5. **Getting Help**:
   - Consult the project documentation
   - Check the source code comments
   - Reach out to the development team

---

This application is showcasing enterprise-grade JavaGIS capabilities with a focus on environmental monitoring and asset tracking. The architecture follows industry best practices and is built with scalability, security, and maintainability in mind.
