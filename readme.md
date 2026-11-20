# Individual & Volunteer Initiative API — Overview

Full technical detail (schemas, params, response codes) lives in the Swagger/OpenAPI UI. This doc is a quick-reference guide to what each endpoint does.

> For detailed documentation, visit **[https://dev.givr.ng/docs](https://dev.givr.ng/docs)**.

## Conventions

- **Auth:** all `/individual/*` endpoints require the `INDIVIDUAL` authority. This is enforced on the controller, so requests from any other account type are rejected.
- **Pagination:** list endpoints marked *paged* accept `pageNum` (default `0`) and `pageSize` (default noted per endpoint) and return a paged response.
- **Enum values:** enum query parameters and fields use the uppercase constant names (for example `DRAFT`, not `draft`).

## Media uploads

Media is uploaded straight to Cloudinary using a short-lived signature from the backend. The signature endpoints (`GET /individual/media/upload/signature`, `GET /volunteer/media/upload/signature`, and the organization equivalent) all work the same way.

**Request:** `purpose` is **required**. It decides the folder, the upload preset (which limits allowed formats and file size) and the resource type.

| `purpose` | Folder | Upload preset | Resource type |
|---|---|---|---|
| `AVATAR` | `givr/avatar` | `givr_avatar` | `image` |
| `ID_PHOTO` | `givr/identity` | `givr_id_photo` | `image` |
| `REPORT_IMAGE` | `givr/reports/images` | `givr_report_image` | `image` |
| `REPORT_VIDEO` | `givr/reports/videos` | `givr_report_video` | `video` |

**Response:** `signature`, `timestamp`, `expiresAt`, `apiKey`, `cloudName`, `folder`, `uploadPreset`, `resourceType`.

**Using it:** `POST https://api.cloudinary.com/v1_1/{cloudName}/{resourceType}/upload` with the file plus `api_key`, `timestamp`, `signature`, `folder` and `upload_preset`, each exactly as returned. The signature covers `timestamp`, `folder` and `upload_preset`, so changing any of them makes the upload fail.

- Request a new signature for every upload. `expiresAt` is 5 minutes after the signature is issued.
- A signature only works for the `purpose` it was issued for. Files that don't meet that preset's format and size limits are rejected by Cloudinary.

## Individual — Initiatives

| Endpoint | What it does |
|---|---|
| `POST /individual/initiative` | Create a new initiative |
| `GET /individual/initiative` | List your initiatives, filtered by `status` (default `DRAFT`). *Paged, default size 20* |
| `PATCH /individual/initiative/{initiativeId}` | Update an initiative |
| `PATCH /individual/initiative/{initiativeId}/publish` | Publish an initiative (draft → live) |
| `PATCH /individual/initiative/{initiative}/completed` | Mark an initiative as completed |
| `DELETE /individual/initiative/{initiativeId}` | Delete an initiative. `DRAFT` initiatives are removed permanently, `OPEN` initiatives are soft-deleted, and any other status is rejected |
| `GET /individual/initiative/participants` | List participants across your initiative(s) |
| `GET /individual/media/upload/signature?purpose=` | Get a signed key for uploading media. See [Media uploads](#media-uploads) |

Initiative responses include `items`, `services` and `media`.

## Individual — Participation & Applications

| Endpoint | What it does |
|---|---|
| `PATCH /individual/initiative/participant` | Update a participant's status/details |
| `GET /individual/initiative/applications` | List volunteer applications to your initiative(s). *Paged, default size 30* |
| `PATCH /individual/initiative/applications/{id}/accept` | Accept a volunteer's application |
| `PATCH /individual/initiative/applications/{id}/reject` | Reject a volunteer's application |

## Individual — Donations

| Endpoint | What it does |
|---|---|
| `GET /individual/donations` | List donations made by you. *Paged, default size 20* |
| `GET /individual/initiative/donations` | List donations across your initiative(s). *Paged, default size 30* |
| `GET /individual/initiative/donations/{donationId}` | Get a single donation |
| `PATCH /individual/initiative/donations/{donationId}` | Update a donation's status |

## Volunteer — Profile

| Endpoint | What it does |
|---|---|
| `POST /volunteer/profile/initiate/verification` | Start account verification for a volunteer |
| `GET /volunteer/media/upload/signature?purpose=` | Get a signed key for uploading media. See [Media uploads](#media-uploads) |

## Volunteer — Projects & Initiatives

| Endpoint | What it does |
|---|---|
| `GET /volunteer/projects` | List open projects available to volunteer on (deadline not yet passed). *Paged, default size 30* |
| `POST /volunteer/projects/apply` | Apply to a project |
| `POST /volunteer/initiative/apply` | Apply to an initiative |
| `GET /volunteer/initiative/{initiativeId}/resources` | Get the resources for an initiative |

### Testing note — volunteer verification

Only the following accounts are usable for testing `initiate/verification`:

| Account No. | Last Name | First Name | Phone |
|---|---|---|---|
| 63184876213 | Bunch | Dillon | 08000000000 |
| 18482561982 | Guion | Audi | 08000000001 |