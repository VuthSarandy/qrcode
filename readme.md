# KHQR Payment Generation Service

## Overview

This project is a Spring Boot application that implements KHQR (Cambodian QR) payment generation and verification. It provides a simple web interface and RESTful APIs to generate QR codes for merchant payments and verify payment statuses.

## Features

- Generate dynamic KHQR codes
- Verify payment status using MD5 hash
- Web interface for QR code generation
- RESTful API endpoints for integration

## Technologies Used

- Spring Boot
- Java
- Bakong KHQR Library
- Bootstrap
- JavaScript

## Prerequisites

- Java 17 or higher
- Maven
- Internet connection (for dependency downloads)

## Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/khqr-payment-service.git
cd khqr-payment-service
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

## API Endpoints

### Generate QR Code

- **URL:** `/api/khqr/generate`
- **Method:** `POST`
- **Request Body:**
```json
{
    "amount": 500.0,
    "currency": "KHR",
    "merchantName": "istad shop",
    "bankAccount": "proeung_chiso@aclb",
    "storeLabel": "My Store",
    "terminalId": "POS-01",
    "isStatic": false
}
```

### Verify Payment

- **URL:** `/api/khqr/verify/{md5Hash}`
- **Method:** `GET`

## Example cURL Request

```bash
curl -X POST http://localhost:8080/api/khqr/generate \
-H "Content-Type: application/json" \
-d '{
    "amount": 500.0,
    "currency": "KHR",
    "merchantName": "istad shop",
    "bankAccount": "proeung_chiso@aclb",
    "storeLabel": "My Store",
    "terminalId": "POS-01",
    "isStatic": false
}'
```

## Configuration

Modify `application.properties` or `application.yml` to configure:
- Server port
- Logging levels
- Any external service configurations

## Dependencies

- Spring Boot Starter Web
- Lombok
- Bakong KHQR Library
- ZXing QR Code Generation Library

## Security Considerations

- Implement proper authentication and authorization
- Use HTTPS in production
- Validate and sanitize all input data

## Troubleshooting

- Ensure all dependencies are correctly imported
- Check network connectivity
- Verify Bakong KHQR library configuration

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Your Name - your.email@example.com

Project Link: [https://github.com/yourusername/khqr-payment-service](https://github.com/yourusername/khqr-payment-service)
