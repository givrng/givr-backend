# Individual & Volunteer Initiative API — Overview

Full technical detail (schemas, params, response codes) lives in the Swagger/OpenAPI UI. This doc is a quick-reference guide to what each endpoint does.

> For detailed documentation, visit **[https://dev.givr.ng/docs](https://dev.givr.ng/docs)**.

## Individual — Initiatives

| Endpoint | What it does |
|---|---|
| `GET /individual/initiative/participants` | List participants across your initiative(s) |
| `GET /individual/initiative/applicants` | List volunteer applications to your initiative(s) |
| `PATCH /individual/initiative/{initiative}/completed` | Mark an initiative as completed |
| `PATCH /individual/initiative/{initiativeId}` | Update an initiative |
| `DELETE /individual/initiative/{initiativeId}` | Delete an initiative |
| `PATCH /individual/initiative/{initiativeId}/publish` | Publish an initiative (draft → live) |
| `POST /individual/initiative` | Create a new initiative |

## Individual — Participation & Applications

| Endpoint | What it does |
|---|---|
| `PATCH /individual/initiative/participant` | Update a participant's status/details |
| `PATCH /individual/initiative/application/{id}/accept` | Accept a volunteer's application |

## Volunteer — Profile

| Endpoint | What it does |
|---|---|
| `POST /volunteer/profile/initiate/verification` | Start account verification for a volunteer |

### Testing note — volunteer verification

Only the following accounts are usable for testing `initiate/verification`:

| Account No. | Last Name | First Name | Phone |
|---|---|---|---|
| 63184876213 | Bunch | Dillon | 08000000000 |
| 18482561982 | Guion | Audi | 08000000001 |