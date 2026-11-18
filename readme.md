# Individual & Volunteer Initiative API — Overview

Full technical detail (schemas, params, response codes) lives in the Swagger/OpenAPI UI. This doc is a quick-reference guide to what each endpoint does.

> For detailed documentation, visit **[https://dev.givr.ng/docs](https://dev.givr.ng/docs)**.

## Conventions

- **Auth:** all `/individual/*` endpoints require the `INDIVIDUAL` role.
- **Pagination:** list endpoints marked *paged* accept `pageNum` (default `0`) and `pageSize` (default noted per endpoint) and return a paged response.

## Individual — Initiatives

| Endpoint | What it does |
|---|---|
| `POST /individual/initiative` | Create a new initiative |
| `GET /individual/initiative` | List your initiatives, filtered by `status` (default `draft`). *Paged, default size 20* |
| `PATCH /individual/initiative/{initiativeId}` | Update an initiative |
| `PATCH /individual/initiative/{initiativeId}/publish` | Publish an initiative (draft → live) |
| `PATCH /individual/initiative/{initiative}/completed` | Mark an initiative as completed |
| `DELETE /individual/initiative/{initiativeId}` | Delete an initiative |
| `GET /individual/initiative/participants` | List participants across your initiative(s) |
| `GET /individual/media/upload/signature` | Get a signed key for uploading media (e.g. initiative images) |

## Individual — Participation & Applications

| Endpoint | What it does |
|---|---|
| `PATCH /individual/initiative/participant` | Update a participant's status/details |
| `GET /individual/initiative/applications` | List volunteer applications to your initiative(s). *Paged, default size 30* |
| `PATCH /individual/initiative/applications/{id}/accept` | Accept a volunteer's application |
| `PATCH /individual/projects/applications/{id}/reject` | Reject a volunteer's application |

## Individual — Donations

| Endpoint | What it does                                                       |
|---|--------------------------------------------------------------------|
| `GET /individual/donations` | List donations made by you. *Paged, default size 20*               |
| `GET /individual/initiative/donations` | List donations across your initiative(s). *Paged, default size 30* |
| `GET /individual/initiative/donations/{donationId}` | Get a single donation                                              |
| `PATCH /individual/initiative/donations/{donationId}` | Update a donation's status                                         |

## Volunteer — Profile

| Endpoint | What it does |
|---|---|
| `POST /volunteer/profile/initiate/verification` | Start account verification for a volunteer |
| `GET /volunteer/media/upload/signature` | Get a signed key for uploading media |

## Volunteer — Projects & Initiatives

| Endpoint | What it does |
|---|---|
| `GET /volunteer/projects` | List projects available to volunteer on. *Paged, default size 30* |
| `POST /volunteer/projects/applyForProject` | Apply to a project |
| `POST /volunteer/initiative/applyForProject` | Apply to an initiative |
| `GET /volunteer/initiative/{initiativeId}/resources` | Get the resources for an initiative |

### Testing note — volunteer verification

Only the following accounts are usable for testing `initiate/verification`:

| Account No. | Last Name | First Name | Phone |
|---|---|---|---|
| 63184876213 | Bunch | Dillon | 08000000000 |
| 18482561982 | Guion | Audi | 08000000001 |