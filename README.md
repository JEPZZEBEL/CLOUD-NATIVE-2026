# Tickets Backend (Semana 6)

Backend Spring Boot securitizado con Azure AD (JWT) e integración con S3 y EFS.

## Local (Docker)

1) Copia `.env.example` a `.env` y completa los valores.
2) Ejecuta:

```bash
docker compose up --build
```

API: `http://localhost:8080`.

## CI/CD

El workflow `.github/workflows/ci-cd.yml` construye, publica la imagen en Docker Hub y despliega en EC2.

En EC2 se espera:
- EFS montado en `/mnt/efs/tickets`
- Archivo `/home/ec2-user/app.env` con variables de entorno
