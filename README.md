## ChoreTracker Backend

Detta är backend-API:et för **ChoreTracker**: en applikation som hjälper hushåll att organisera sysslor kollaborativt! Användare kan skapa eller gå med i hushåll, skapa återkommande uppgifter med poäng och spåra vem som gör sin del av arbetet.

**Hur det fungerar:**

- Användare registrerar sig och skapar eller går med i ett hushåll med hjälp av en inbjudningskod.
- Hushållsägare och medlemmar kan skapa uppgifter med anpassningsbara poäng och frekvens.
- När medlemmar slutför uppgifter tjänar de poäng och uppgiften schemaläggs automatiskt om baserat på dess frekvens.
- Hushållet spårar medlemmarnas poäng för att se vem som bidrar mest.
- Premiumfunktioner finns tillgängliga genom Stripe-integration för utökad funktionalitet.

Detta backend-api bidrar med:

- Autentisering & användarhantering
- Hushållshantering med inbjudningskoder
- Skapa uppgifter, spåra slutförande och automatisk omschemaläggning
- Poängspårning för hushållsmedlemmar
- Stripe-betalningsintegration för premiumfunktioner

## Verktyg

- Java 21
- Spring Boot 3.5.6
- MongoDB
- Maven
- Spring Security (JWT)
- Stripe API

## Installation

Klona repot:

```bash
git clone git@github.com:adNord/SideQuest-ChoreTracker.git
```

Skapa en `.env`-fil i rotmappen med följande variabler:

```bash
MONGODB_URI='din-mongo-anslutning'
MONGODB_DATABASE='choretracker'
JWT_SECRET_KEY='din-jwt-hemlighet'
STRIPE_SECRET_KEY='din-stripe-hemliga-nyckel'
STRIPE_PRICE_ID='ditt-stripe-pris-id'
STRIPE_SUCCESS_URL='din-success-url'
STRIPE_CANCEL_URL='din-cancel-url'
STRIPE_WEBHOOK_SECRET='din-webhook-hemlighet'
```

Starta API:et med:

```bash
./mvnw spring-boot:run
```

## API-endpoints

### Auth - /api/auth

- `POST /api/auth/register` - Användarregistrering

RequestBody: 
```json
{
    "username": "användarnamn",
    "password": "lösenord",
    "passwordConfirm": "lösenord"
}
```

- `POST /api/auth/login` - Användarautentisering

RequestBody:
```json
{
    "username": "användarnamn",
    "password": "lösenord"
}
```
Returnerar:
```json
{
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.blablablablabla"
}
```

### Household - /api/household

- `POST /api/household` - Skapa ett nytt hushåll

RequestBody: 
```json
{
    "name": "Mitt Hushåll"
}
```
Returnerar hushållsobjekt med genererad inbjudningskod.

- `POST /api/household/join` - Gå med i ett befintligt hushåll

RequestBody:
```json
{
    "inviteCode": "cheerful-panda-42"
}
```

- `POST /api/household/leave` - Lämna ett hushåll

RequestBody:
```json
{
    "householdId": "householdId"
}
```

- `GET /api/household` - Hämta användarens hushållsdetaljer med medlemspoäng

Returnerar:
```json
{
    "id": "householdId",
    "name": "Mitt Hushåll",
    "ownerId": "userId",
    "members": [
        {
            "user": {
                "id": "userId",
                "username": "användarnamn"
            },
            "score": 150
        }
    ],
    "inviteCode": "cheerful-panda-42",
    "isPremium": false
}
```

- `PATCH /api/household/{householdId}/reset-scores` - Återställ alla medlemspoäng (endast ägare)

### Tasks - /api/task

- `POST /api/task` - Skapa en ny uppgift

RequestBody: 
```json
{
    "title": "Ta ut soporna",
    "score": 10,
    "frequencyDays": 3
}
```

- `GET /api/task` - Hämta alla uppgifter för användarens hushåll

Returnerar lista med uppgifter:
```json
[
    {
        "id": "taskId",
        "householdId": "householdId",
        "title": "Ta ut soporna",
        "score": 10,
        "frequencyDays": 3,
        "dueDate": "2025-11-03T12:00:00Z"
    }
]
```

- `PATCH /api/task/{taskId}` - Markera uppgift som slutförd

Slutför uppgiften, lägger till poäng till användaren och schemaläger om baserat på frekvens.

- `DELETE /api/task/{taskId}` - Ta bort en uppgift

### Stripe - /api/stripe

- `POST /api/stripe/create-checkout-session` - Skapa Stripe checkout-session för premiumuppgradering

Returnerar:
```json
{
    "url": "https://checkout.stripe.com/..."
}
```

- `POST /api/stripe/webhook` - Stripe webhook-endpoint för betalhändelser

## Funktioner

- **Säker Autentisering**: JWT-baserad autentisering med Spring Security
- **Hushållshantering**: Skapa hushåll med unika inbjudningskoder (format: adjektiv-substantiv-nummer)
- **Återkommande Uppgifter**: Uppgifter schemaläggs automatiskt om baserat på anpassad frekvens
- **Poängsystem**: Spåra bidrag med poängbaserad poängsättning
- **Premiumtjänst**: Stripe-integration för premiumfunktioner
- **Realtidsuppdateringar**: WebSocket-stöd för live-uppdateringar (kommer snart)

## Frontend
[ChoreTracker Frontend](https://github.com/adNord/SideQuest-ChoreTracker-frontend)
