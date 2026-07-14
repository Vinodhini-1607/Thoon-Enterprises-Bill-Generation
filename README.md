# Bill Generation System

A standalone application for generating bills with GST calculation for construction material supply companies.

## Technology Stack

### Backend
- **Spring Boot 3.2.0** with Java 17+
- **H2 Database** (embedded, file-based persistence)
- **Spring Data JPA** with Hibernate
- **Maven** for dependency management

### Frontend
- **Angular 17+** (Single Page Application)
- **Bootstrap 5** for UI styling
- **RxJS** for reactive programming
- **TypeScript**

## Features

- **Customer Management**: Add, edit, delete, and search customers
- **Product Management**: Manage products with GST rates and HSN codes
- **Bill Generation**: Create bills with automatic GST calculation (CGST + SGST/IGST)
- **Real-time Calculations**: Automatic bill total calculation
- **Reporting**: Monthly sales, customer-wise, product-wise, and GST reports
- **PDF Export**: Generate professional PDF bills (to be implemented)

## Project Structure

```
BILL_GENERATION/
├── backend/                 # Spring Boot Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/billgen/
│   │   │   │   ├── entity/          # JPA Entities
│   │   │   │   ├── repository/      # Data Access Layer
│   │   │   │   ├── service/         # Business Logic
│   │   │   │   ├── controller/      # REST APIs
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   └── config/          # Configuration
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── pom.xml
│
└── frontend/                # Angular Frontend
    ├── src/
    │   ├── app/
    │   │   ├── models/              # TypeScript Interfaces
    │   │   ├── services/            # HTTP Services
    │   │   ├── components/          # Feature Components
    │   │   │   ├── dashboard/
    │   │   │   ├── customers/
    │   │   │   ├── products/
    │   │   │   ├── bills/
    │   │   │   └── reports/
    │   │   ├── app.module.ts
    │   │   ├── app-routing.module.ts
    │   │   └── app.component.ts
    │   ├── index.html
    │   └── styles.css
    ├── package.json
    ├── angular.json
    └── tsconfig.json
```

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Node.js 18 or higher
- Maven 3.6 or higher

### To Start the Application using the JAR
 1) Navigate to /backend/target folder
 2) copy the file bill-generation-backend-1.0.0.jar 
 3) put it in a desired location
 4) open the command prompt 
 5) Run the below command to start the application
 6) java -jar bill-generation-backend-1.0.0.jar
 7) Once the application started . Navigate to http://localhost:8080/ to access the application
 8) Database can be accessed via http://localhost:8080/h2-console/
    

### Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
ng serve
```

The frontend will start on `http://localhost:4200`

### H2 Database Console

Access the H2 database console at:
```
http://localhost:8080/h2-console
```

**Connection Details:**
- JDBC URL: `jdbc:h2:file:./data/billgen`
- Username: `sa`
- Password: (leave empty)

## API Endpoints

### Customers
- `GET /api/customers` - Get all customers
- `GET /api/customers/{id}` - Get customer by ID
- `GET /api/customers/search?keyword={keyword}` - Search customers
- `POST /api/customers` - Create new customer
- `PUT /api/customers/{id}` - Update customer
- `DELETE /api/customers/{id}` - Delete customer

### Products
- `GET /api/products` - Get all products
- `GET /api/products/{id}` - Get product by ID
- `GET /api/products/search?keyword={keyword}` - Search products
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Bills
- `GET /api/bills` - Get all bills
- `GET /api/bills/{id}` - Get bill by ID
- `GET /api/bills/number/{billNumber}` - Get bill by number
- `GET /api/bills/customer/{customerId}` - Get bills by customer
- `GET /api/bills/date-range?startDate={start}&endDate={end}` - Get bills by date range
- `POST /api/bills` - Create new bill
- `DELETE /api/bills/{id}` - Delete bill

### Reports
- `GET /api/reports/monthly-sales?year={year}&month={month}` - Monthly sales report
- `GET /api/reports/customer-wise?customerId={id}&startDate={start}&endDate={end}` - Customer-wise report
- `GET /api/reports/product-wise?startDate={start}&endDate={end}` - Product-wise report
- `GET /api/reports/gst?startDate={start}&endDate={end}` - GST report

## GST Calculation

The system supports:
- **Intra-state transactions**: CGST + SGST (split equally)
- **Inter-state transactions**: IGST (single tax)

GST rates are configurable per product and automatically calculated during bill generation.

## Deployment

### Single JAR Deployment

1. Build the Angular app:
```bash
cd frontend
ng build --prod
```

2. Copy the built files to Spring Boot resources:
```bash
cp -r dist/* ../backend/src/main/resources/static/
```

3. Build the Spring Boot JAR:
```bash
cd ../backend
mvn clean package
```

4. Run the JAR:
```bash
java -jar target/bill-generation-backend-1.0.0.jar
```

The application will be available at `http://localhost:8080`

## Database

The application uses H2 embedded database with file-based persistence. Data is stored in:
```
backend/data/billgen.mv.db
```

## Future Enhancements

- PDF bill generation and download
- Excel export for reports
- User authentication and authorization
- Multi-currency support
- Barcode/QR code generation for bills
- Email bill delivery
- Dashboard analytics and charts
- Inventory management integration

## License

This project is created for educational and commercial use.
