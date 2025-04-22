# Blood Pressure Tracker - Dual Interface Approach

## Overview

The Blood Pressure Tracker application is designed to support two different user interfaces:

1. **Web Interface**: Using Thymeleaf templates for server-side rendering
2. **Mobile Interface**: Using Flutter with a REST API backend

This document explains the dual approach and provides guidance on maintaining both interfaces.

## Architecture

### Web Interface (Thymeleaf)

The web interface uses traditional Spring MVC with Thymeleaf templates:

- Controllers in `org.chikere.bptracker.app.controller` package
- Thymeleaf templates in `src/main/resources/templates`
- Server-side rendering of HTML pages
- Session-based authentication with Spring Security

### Mobile Interface (Flutter with REST API)

The mobile interface uses Flutter with a REST API backend:

- REST controllers in `org.chikere.bptracker.app.controller.api` package
- JSON responses instead of HTML views
- JWT-based authentication
- CORS configuration to allow cross-origin requests

## Why Keep Both Approaches?

There are several benefits to maintaining both interfaces:

1. **Broader Accessibility**: Users can access the application from both web browsers and mobile devices
2. **Different Use Cases**: Web interface may be better for administrative tasks, while mobile is better for on-the-go data entry
3. **Gradual Migration**: Allows for a gradual migration from web to mobile without disrupting existing users
4. **Flexibility**: Different user groups may prefer different interfaces

## HTML Templates

The HTML templates in `src/main/resources/templates` are still required for the web interface to function properly. Removing these files would break the web interface functionality.

If you decide to fully migrate to a mobile-only approach in the future, you would need to:

1. Remove the Thymeleaf templates
2. Remove or refactor the traditional controllers
3. Remove Thymeleaf dependencies from pom.xml
4. Update the security configuration to handle only API requests

## Maintenance Considerations

When maintaining both interfaces, keep in mind:

1. **Consistent Business Logic**: Ensure that business logic is consistent across both interfaces
2. **Feature Parity**: New features should be implemented for both interfaces when appropriate
3. **Testing**: Test both interfaces thoroughly when making changes
4. **Documentation**: Document which features are available in which interface

## Conclusion

The current dual approach provides flexibility and broader accessibility for users. The HTML templates should be kept as long as you want to maintain the web interface functionality.