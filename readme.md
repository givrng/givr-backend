# Givr API Documentation (v0)

REST API for the Givr volunteering platform. It connects **volunteers** with **organizations** (projects) and **individuals** (initiatives that need items, services, or time).

- **Base URL (dev):** `https://dev.givr.ng`
- **API prefix:** `/v1/api`
- **Spec:** OpenAPI 3.1.0, version `v0`
- **Format:** JSON request and response bodies (`Content-Type: application/json`)

## 1. Conventions

### Roles and path namespaces

| Role | Prefix | Purpose |
| --- | --- | --- |
| Volunteer | `/v1/api/volunteer` | Browse and apply to projects and initiatives, manage profile, track volunteering, review projects |
| Organization | `/v1/api/organization` | Create and publish projects, manage applicants and participants, broadcast messages |
| Individual | `/v1/api/individual` | Create and manage initiatives, handle applications and donations |
| Admin | `/v1/api/admin` | Verify organizations, review verification sessions, approve certificates |
| Shared | `/v1/api/password`, `/v1/api/auth`, `/v1/api/certificates` | Password reset, token refresh, public certificate verification |

### Authentication

The spec declares **no security scheme**, so the mechanism is not formally documented. Endpoints such as `GET /v1/api/auth/refresh` and `POST .../logout` indicate token- or session-based auth issued at signup/login. Confirm the exact mechanism (cookie vs. `Authorization: Bearer`) with the backend team and add it to the spec as a `securitySchemes` entry. Role-prefixed endpoints should be treated as requiring an authenticated user of that role.

### Pagination

Paged endpoints take `pageNum` (default `0`) and `pageSize`, and return:

```json
{
  "content": [ /* items */ ],
  "page": { "size": 30, "number": 0, "totalElements": 120, "totalPages": 4 }
}
```

Some endpoints name the parameters differently: organization participants use `pageNumber`/`pageSize` (default 40), admin pending certificates use `page`/`size` (default 30), chat history uses `cursor`/`size` (default 40).

### Responses

Almost every endpoint documents only `200 OK`. Error shapes are not specified.

### OTP purposes

`purpose` query value: `EMAIL_VERIFICATION`, `PASSWORD_UPDATE`, `ADMIN_AUTH`.

## 2. Authentication and account flows

| Method | Path                                 | Description | Body |
| --- |--------------------------------------| --- | --- |
| POST | `/volunteer/auth/signup`             | Create volunteer account | `CreateVolunteerRequestDto` |
| POST | `/organization/auth/signup`          | Create organization account | `CreateOrganizationDto` |
| POST | `/volunteer/otp/request?purpose=`    | Send OTP to the volunteer's email | none |
| POST | `/organization/otp/request?purpose=` | Send OTP to the organization's email | none |
| PATCH | `/volunteer/verify/email`            | Confirm volunteer email with OTP | `OtpDto` |
| PATCH | `/organization/verify/email`         | Confirm organization email with OTP | `OtpDto` |
| POST | `/volunteer/logout`                  | Log out volunteer | none |
| POST | `/organization/logout`               | Log out organization | none |
| GET | `/volunteer/me/details`              | Current volunteer's `userId` and `email` | none |
| GET | `/organization/me/details`           | Current organization's `userId` and `email` | none |
| GET | `/auth/refresh`                      | Refresh the auth token | none |
| POST | `/password/forgot`                   | Request password-reset OTP | `OtpRequestDto` |
| POST | `/password/reset`                    | Reset password using OTP | `PasswordResetDto` |
| PATCH | `/volunteer/password/update`         | Change volunteer password | `PasswordUpdateDto` |
| PATCH | `/organization/password/update`      | Change organization password | `PasswordUpdateDto` |

All paths above are relative to `/v1/api`.

### Example: volunteer signup

```http
POST /v1/api/volunteer/auth/signup
Content-Type: application/json

{
  "firstname": "Ada",
  "lastname": "Obi",
  "email": "ada@example.com",
  "phone": "08012345678",
  "password": "s3cret!",
  "confirmPassword": "s3cret!",
  "location": { "state": "Lagos", "lga": "Ikeja" },
  "interests": ["Education", "Health"]
}
```

### Example: password reset

```http
POST /v1/api/password/forgot
{ "email": "ada@example.com", "role": "VOLUNTEER" }

POST /v1/api/password/reset
{ "email": "ada@example.com", "role": "VOLUNTEER", "otp": "123456", "newPassword": "n3wPass!" }
```

## 3. Account verification

Verification checks a person's or organization's identity against a government ID. A verified volunteer can create initiatives and apply to them; an organization is verified after an admin review.

### Accepted ID types

Only two ID types are accepted for `idType`, for volunteers and for an organization's contact person: `NIN` and `DL`. The OpenAPI spec still lists `VOTER_CARD`, `PASSPORT` and `vNIN`, but the server enum no longer includes them, so requests using those values fail.

| `idType` | ID | Notes |
| --- | --- | --- |
| `NIN` | National Identification Number | The only type usable in testing (section 12) |
| `DL` | Driver's licence | Not usable in testing |

### Callback URL

Every verification initialization requires a `callbackUrl`, for volunteers and organizations alike: a valid URL in your app (for example, a "verification complete" page) that the user is sent back to after the payment checkout. It is passed to the payment provider when the checkout is created, so each call can use a different value.

- It is required in the request body of `POST /volunteer/profile/initiate/verification` and `POST /organization/verification/initiate`. A missing, blank or malformed value fails validation.
- The redirect only means the user has left the payment page. It does not mean verification has passed; read the status from the profile endpoints described below.

### Volunteer verification

The volunteer pays a verification fee, then the ID is checked automatically with the ID provider (QoreID).

1. Log in as the volunteer and collect the ID details. The names must match the ID.
2. Optional: get an upload signature (`GET /volunteer/media/upload/signature`), upload a photo, and send its URL as `individualLogo`.
3. Call `POST /volunteer/profile/initiate/verification` with a `VolunteerVerificationDto`, including a `callbackUrl` (see above). The server saves a verification session and returns `{ "checkoutUrl": "..." }`.
4. Send the user to `checkoutUrl` to pay. The fee amount is set in server configuration. After checkout, the user is redirected to `callbackUrl`.
5. Once payment is confirmed, the backend runs the ID check asynchronously. A successful QoreID response marks both the session and the volunteer `VERIFIED`.
6. Poll `GET /volunteer/profile` and read `verificationStatus`. It is absent before the first attempt, and `VERIFIED` is the only value that unlocks initiatives.

Calling the endpoint again before payment completes returns a fresh `checkoutUrl`, created with the `callbackUrl` from the new request. Once a payment exists and the volunteer already has a `verificationStatus`, the call fails with "Verification has been paid for, await verification update from admin".

### Organization verification

1. Sign up and complete the profile with `PATCH /organization/profile`: name, `cacRegNumber`, `cacDocUrl`, address and contact details.
2. Call `POST /organization/verification/initiate` with an `OrganizationUpdateDto` that includes `contactVerification` (`idType` of `NIN` or `DL`, `idNumber`, `usrImgUrl`) and a `callbackUrl` (see above). It returns a `checkoutUrl` for the fee; after checkout the user is redirected to `callbackUrl`.
3. The ID check runs automatically with the contact person's ID number and names, using the `AUTOMATIC_VERIFICATION_*` statuses.
4. An admin lists sessions with `GET /admin/verifications/organization?status=Pending`, then approves or rejects (type "Organization") with `POST /admin/review`. `PATCH /admin/review/{sessionId}/retry` re-runs the automatic check.
5. Read the outcome from `GET /organization/profile`: `organization.status` and `organization.reviewStatus`.

### Admin view of volunteer sessions

`GET /admin/verifications/individual` lists volunteer verification sessions, paged and newest first, as `VolunteerVerificationSessionDto`. Admins record a decision with POST /admin/review and type "Individual". That stores the reason and sets the session's reviewStatus, but it does not change the volunteer's verificationStatus or send an email; VERIFIED still comes from the QoreID result. No endpoint reads a volunteer session's review back.

### What verification unlocks

- `POST /volunteer/initiative/apply` is for `VERIFIED` volunteers only. A volunteer with no status fails with "Account not verified, only verified accounts can participate in initiatives". One with another status, such as `PENDING`, fails with "Only verified volunteers can apply to initiatives".
- Creating and managing initiatives (`/individual/*`) is for verified volunteers.
- Status values: `PENDING` means a session was submitted or resubmitted and the check has not finished; `VERIFIED` means the ID check passed. `UNVERIFIED` and the `AUTOMATIC_VERIFICATION_*` values belong to the organization flow.

### Example: start volunteer verification

```http
POST /v1/api/volunteer/profile/initiate/verification
Content-Type: application/json

{
  "idType": "NIN",
  "idNumber": "<NIN>",
  "firstname": "Ada",
  "middleName": "Chioma",
  "lastname": "Obi",
  "dateOfBirth": "1995-04-12",
  "callbackUrl": "https://app.example.com/verification/complete"
}
```

```json
{ "checkoutUrl": "https://..." }
```

### Testing

In the dev environment, verification only succeeds for the two NIN test accounts listed in section 12. Use `idType` `NIN` and the details given there.

## 4. Volunteer endpoints

Prefix: `/v1/api/volunteer`

### Profile and verification

| Method | Path | Description | Request | Response |
| --- | --- | --- | --- | --- |
| GET | `/profile` | Get own profile | none | `VolunteerProfile` |
| PATCH | `/profile` | Update profile | `UpdateVolunteerDto` | `VolunteerProfile` |
| GET | `/profile/image?userId=` | Get a profile image URL | query `userId` (required) | URL string |
| POST | `/profile/initiate/verification` | Start ID verification; returns a checkout/verification URL. `callbackUrl` is required (section 3) | `VolunteerVerificationDto` | `CheckoutResponse` |
| GET | `/dashboard` | Dashboard summary | none | `VolunteerDashboard` |
| GET | `/media/upload/signature` | Signed params for direct media upload (cloud storage) | none | `MediaUploadResponse` |

### Discovering projects and organizations

| Method | Path | Description | Response |
| --- | --- | --- | --- |
| GET | `/projects?pageNum=&pageSize=` | Available projects (default size 30) | `PagedModelProjectResponseDto` |
| GET | `/projects/{id}` | Project detail | `ProjectResponseDto` |
| GET | `/projects/recommended` | Recommended projects | `ProjectResponseDto[]` |
| GET | `/projects/unread` | Unread chat message counts, map of project ID to count | `{ "<projectId>": <int64> }` |
| GET | `/organizations` | List organizations | `OrganizationResponseDTOv[]` |
| GET | `/organizations/{orgId}/projects/active` | An organization's active projects | `ProjectViewResponse[]` |
| GET | `/share/project/{id}` | Shareable link for a project | string |
| GET | `/initiative/{initiativeId}/resources` | Items, services, and media an initiative needs | `InitiativeResourceResponseDto` |

### Applying and participating

| Method | Path                            | Description | Request |
| --- |---------------------------------| --- | --- |
| POST | `/projects/apply`               | Apply to a regular project (initiative IDs are rejected) | `ProjectApplicationForm` |
| POST | `/initiative/apply`             | Apply to an initiative (time and/or items); `applicationType` selects which of `items` and `services` is read (section 9); verified volunteers only (section 3), and project IDs are rejected | `InitiativeApplicationForm` |
| GET | `/volunteering`                 | List own participations | returns `ParticipationDto[]` |
| DELETE | `/volunteering/{participationId}` | Withdraw from / reject a participation | none |
| POST | `/volunteering/{participationId}/review` | Rate a completed project | `RatingDTO` |
| GET | `/chat/{projectId}/history?cursor=&size=` | Project chat history | returns `PagedModelGivrMessage` |

### Example: apply to a project

```http
POST /v1/api/volunteer/projects/apply

{
  "projectId": 42,
  "reason": "I want to support literacy in my community.",
  "aboutMe": "Secondary school teacher with 5 years of experience.",
  "isAvailable": true,
  "mySkills": ["Teaching", "Public speaking"],
  "phoneNumber": "08012345678"
}
```

### Example: apply to an initiative with items

```http
POST /v1/api/volunteer/initiative/apply

{
  "projectId": 7,
  "reason": "Happy to donate books.",
  "aboutMe": "Local bookstore owner.",
  "isAvailable": false,
  "applicationType": "Items_Only",
  "items": [ { "resourceId": "res_123", "resource": "Textbooks", "quantity": 50 } ]
}
```

## 5. Organization endpoints

Prefix: `/v1/api/organization`

### Profile and verification

| Method | Path | Description | Request | Response |
| --- | --- | --- | --- | --- |
| GET | `/profile` | Get organization profile | none | `OrganizationProfileDto` |
| PATCH | `/profile` | Update organization profile | `OrganizationUpdateDto` | `OrganizationProfileDto` |
| GET | `/profile/image?userId=` | Profile image URL | query `userId` | string |
| GET | `/profile/email/exists?email=` | Check whether an email is registered | query `email` | `EmailExists` |
| POST | `/verification/initiate` | Submit CAC/contact details and start verification. `callbackUrl` is required (section 3) | `OrganizationUpdateDto` | `CheckoutResponse` |
| GET | `/dashboard` | Dashboard: projects grouped by status, rating, application stats | none | `OrganizationDashboard` |
| GET | `/media/upload/signature` | Signed media-upload params | none | `MediaUploadResponse` |

### Projects

| Method | Path | Description | Request | Response |
| --- | --- | --- | --- | --- |
| GET | `/projects` | List own projects | none | `ProjectResponseDto[]` |
| POST | `/projects` | Create a project | `ProjectRequestDto` | `ProjectResponseDto[]` |
| PATCH | `/projects/{projectId}` | Update a project | `ProjectRequestDto` | `ProjectResponseDto` |
| DELETE | `/projects/{projectId}` | Delete a project | none | none |
| PATCH | `/projects/{projectId}/publish` | Publish a draft project | none | none |
| PATCH | `/projects/{projectId}/completed` | Mark project completed | none | none |
| GET | `/share/project/{id}` | Shareable link | none | string |

### Applicants, participants, and communication

| Method | Path | Description | Request | Response |
| --- | --- | --- | --- | --- |
| GET | `/projects/applicants` | List volunteer applications | none | `VolunteerApplicationDto[]` |
| PATCH | `/projects/application/{id}/accept` | Accept an application | none | none |
| PATCH | `/projects/application/{id}/reject` | Reject an application | none | none |
| GET | `/projects/participants?pageNumber=&pageSize=` | List participants (default size 40) | none | `PagedModelParticipationDto` |
| PATCH | `/projects/participant` | Update a participant's status | `UpdateParticipantDto` | none |
| POST | `/projects/broadcast` | Message all participants of a project | `BroadcastMsgDto` | none |
| GET | `/projects/unread` | Unread chat counts per project | none | map |
| GET | `/chat/{projectId}/history?cursor=&size=` | Project chat history | none | `PagedModelGivrMessage` |

### Example: create a project

```http
POST /v1/api/organization/projects

{
  "title": "Community Clean-up",
  "description": "Weekend clean-up drive.",
  "categories": ["Environment"],
  "maxVolunteers": 30,
  "location": { "state": "Lagos", "lga": "Surulere" },
  "address": "12 Adeniran Ogunsanya St",
  "startDate": "2026-12-05",
  "endDate": "2026-12-06",
  "applicationDeadline": "2026-12-01",
  "attendanceHours": { "from": "09:00", "to": "15:00" },
  "requiredSkills": ["Teamwork"]
}
```

Required fields: `title`, `categories`, `location`, `address`, `startDate`, `endDate`, `applicationDeadline`, `attendanceHours`, `requiredSkills`. Newly created projects are expected to start in `DRAFT` until published.

## 6. Individual endpoints (initiatives)

Prefix: `/v1/api/individual`

Initiative creation and management are for volunteers whose `verificationStatus` is `VERIFIED` (section 3).

| Method | Path                                          | Description                                                                                       | Request                | Response                            |
|--------|-----------------------------------------------|---------------------------------------------------------------------------------------------------|------------------------|-------------------------------------|
| GET    | `/initiative?status=&pageNum=&pageSize=`      | List own initiatives. `status`: `DRAFT` (default), `OPEN`, `CLOSE`, `ONGOING`, `COMPLETED`, `ALL` | none                   | `PagedModelInitiativeResponseDto`   |
| POST   | `/initiative`                                 | Create an initiative                                                                              | `InitiativeRequestDto` | `InitiativeResponseDto[]`           |
| PATCH  | `/initiative/{initiativeId}`                  | Update an initiative                                                                              | `InitiativeRequestDto` | `InitiativeResponseDto`             |
| DELETE | `/initiative/{initiativeId}`                  | Delete an initiative                                                                              | none                   | none                                |
| PATCH  | `/initiative/{initiativeId}/publish`          | Publish                                                                                           | none                   | none                                |
| PATCH  | `/initiative/{initiative}/completed`          | Mark completed                                                                                    | none                   | none                                |
| GET    | `/initiative/participants`                    | List participants                                                                                 | none                   | `ParticipationDto[]`                |
| PATCH  | `/initiative/participant`                     | Update participant status                                                                         | `UpdateParticipantDto` | none                                |
| GET    | `/initiative/applications?pageNum=&pageSize=` | Volunteer applications                                                                            | none                   | `PagedModelVolunteerApplicationDto` |
| PATCH  | `/initiative/applications/{id}/accept`        | Accept an application                                                                             | none                   | none                                |
| PATCH  | `/initiative/applications/{id}/reject`        | Reject an application                                                                             | none                   | none                                |
| GET    | `/initiative/donations?pageNum=&pageSize=`    | List donations made to account owner initiatives                                                  | none                   | `PagedModelDonationDto`             |
| GET    | `/initiative/donations/{donationId}`          | Donation detail                                                                                   | none                   | `DonationDto`                       |
| GET    | `/individual/donations?pageNum=&pageSize=`    | Lists donations made by account owner                                                             | none                   | `PagedModelDonationDto`             |
| PATCH  | `/initiative/donations/{donationId}`          | Update donation status                                                                            | `DonationUpdateDto`    | `DonationDto`                       |
| GET    | `/media/upload/signature`                     | Signed media-upload params                                                                        | none                   | `MediaUploadResponse`               |

Initiatives extend the project model with `initiativeItemList`, `initiativeServiceList` (both `InitiativeResourceDto[]`) and a required `media` object (`{ mediaType: "Video", mediaUrl }`). When an initiative is updated, a non-empty `media.mediaUrl` replaces its current media, so include the `media` object in update requests.

## 7. Admin endpoints

Prefix: `/v1/api/admin`. Admin login is OTP-based.

| Method | Path | Description | Request | Response |
| --- | --- | --- | --- | --- |
| POST | `/auth/request-otp` | Request an admin OTP | `AdminAuthDto` (`email`) | none |
| POST | `/auth/verify-otp` | Authenticate with OTP | `AdminAuthDto` (`email`, `otp`) | none |
| GET | `/verify` | Check admin session/authorization | none | none |
| DELETE | \`\` | Remove (log out) the admin | none | none |
| GET | `/verifications/organization?pageNum=&pageSize=&status=` | List organization verification sessions (paged, 30 per page by default); `status`: `Approved`, `Rejected`, `Pending` | none | `PagedModelVerificationSessionDto` |
| GET | `/verifications/individual?pageNum=&pageSize=&status=` | List volunteer verification sessions (paged, 30 per page by default, newest first) | none | `PagedModelVolunteerVerificationSessionDto` |
| POST | `/review` | Approve or reject a verification session; type selects Organization or Individual | `ReviewDto` | `ReviewResponseDto` |
| GET | `/review/{sessionId}` | Latest review for an organization session | none | `ReviewResponseDto` |
| PATCH | `/review/{sessionId}/retry` | Re-run automatic verification (organization sessions) | none | none |
| GET | `/certificate/pending?page=&size=` | Participants awaiting certificates | none | `PagedModelParticipationDto` |
| PATCH | `/certificate/{participantId}/approve` | Generate one certificate | none | none |
| PATCH | `/certificate/approve` | Generate certificates in batch | `BatchCertificateRequest` | none |

### Example: review a verification session

```http
POST /v1/api/admin/review
{ "sessionId": 118, "type": "Organization", "isApproved": false, "reason": "CAC number does not match the uploaded document." }
```

## 8. Public endpoint

| Method | Path | Description | Response |
| --- | --- | --- | --- |
| GET | `/v1/api/certificates/verify/{certId}` | Verify a volunteer certificate by ID | `VolunteerCertificateVerificationDTO` |

## 9. Data models

### Requests

**CreateVolunteerRequestDto** (required: `firstname`, `lastname`, `location`, `password`, `phone`, `interests`) `firstname`, `middleName`, `lastname`, `email` (email format), `phone` (11-13 chars), `password` / `confirmPassword` (min 6), `location` (`Location`: `id`, `state`, `lga`), `profileUrl`, `interests` (string\[\]).

**CreateOrganizationDto** (required: `contactFirstname`, `contactLastname`, `password`) `email`, `password` (min 6), `contactFirstname`, `contactMiddleName`, `contactLastname`.

**UpdateVolunteerDto**: `firstname`, `middleName`, `lastname`, `profileUrl`, `phoneNumber` (11-13), `skills[]`, `location` (`LocationDto`), `email`.

**VolunteerVerificationDto** (required: `firstname`, `lastname`, `idType`, `idNumber`, `callbackUrl`) `idType` (`DL` or `NIN`; see section 3), `idNumber`, `firstname`, `middleName`, `lastname`, `dateOfBirth` (date; stored but not sent to the ID provider), `individualLogo`, `callbackUrl` (valid URL the user is redirected to after payment; see section 3).

**OrganizationUpdateDto**: `name`, `category[]`, `location`, `profileUrl`, `status`, `cacRegNumber`, `description`, `website`, `address`, `cacDocUrl`, `email`, `contactFirstname`, `contactLastname`, `contactMiddleName`, `contactPersonProfileUrl`, `dateOfBirth`, `phoneNumber` (11-13), `contactVerification` (`idType`, `idNumber`, `usrImgUrl`), `callbackUrl` (required to initialize verification with `POST /organization/verification/initiate`; must be a valid URL; see section 3).

**ProjectRequestDto** / **InitiativeRequestDto**: `title`, `description`, `categories[]`, `maxVolunteers` (min 1), `location`, `address`, `startDate`, `endDate`, `applicationDeadline`, `attendanceHours` (`from`, `to`), `requiredSkills[]`, `specialRequirements`, `projectFlierUrl`. `locationDto` and `status` were removed from the request. Initiatives add `initiativeItemList[]`, `initiativeServiceList[]` and a required `media` object, and a new initiative always starts as `DRAFT`. Title uniqueness is now per creator, not global.

**ProjectApplicationForm** (required: `projectId`, `reason`, `aboutMe`, `isAvailable`) `projectId`, `reason`, `aboutMe`, `isAvailable`, `mySkills[]`, `additionalInfo`, `phoneNumber` (11-16).

**InitiativeApplicationForm**: everything in `ProjectApplicationForm` plus `applicationType` (required: `Items_Only`, `Items_and_time`, `Time`), `items[]`, `services[]` (each `InitiativeResourceDto`: `resource`, `resourceId`, `quantity`). `applicationType` decides which lists are read: `Items_Only` reads `items`, `Time` reads `services`, and `Items_and_time` reads both. Send the list or lists that the chosen type reads; any other list is ignored.

**Other request bodies:** `RatingDTO` (`rating`: double), `OtpDto` (`otp`), `OtpRequestDto` (`email`, `role`), `PasswordResetDto` (`email`, `role`, `newPassword`, `otp`; `otp` required), `PasswordUpdateDto` (`otp`, `password`, `rePassword`), `UpdateParticipantDto` (`id`, `status`), `BroadcastMsgDto` (`projectId`, `message`), `DonationUpdateDto` (`donationId`, `status`; both required), `ReviewDto` (`sessionId`, `isApproved`, `reason`, `type`: `Organization` or `Individual`; all required), `BatchCertificateRequest` (`participants`: int64\[\]), `AdminAuthDto` (`email`, `otp`).

### Responses

**ProjectResponseDto**: `id`, `title`, `organization` (`OrganizationDto`), `description`, `categories[]`, `maxVolunteers`, `location`, `startDate`, `endDate`, `applicationDeadline`, `attendanceHours`, `requiredSkills[]`, `specialRequirements`, `totalApplicants`, `status`, `createdAt`, `updatedAt`, `address`, `broadcastEnabled`, `rating`, `projectFlierUrl`, `projectCardUrl`, `type` (`Organization` or `Individual`). `InitiativeResponseDto` has the same shape.

`type` says who created the listing: `Organization` for an organization's project and `Individual` for an initiative created by a volunteer. These values replace the earlier `Project` and `Initiative`, so clients that branch on `type` must be updated.

**VolunteerProfile**: `id`, `firstname`, `middleName`, `lastname`, `profileUrl`, `phoneNumber`, `skills[]`, `location`, `certificates[]` (`VolunteerCertificateDto`), `email`, `emailEditable`, `emailIsVerified`, `phoneIsVerified`, `createdAt`, `verificationStatus`.

**VolunteerCertificateDto**: `certId`, `certUrl`, `organizationName`, `projectTitle`, `issuedAt`.

**ParticipationDto**: `id`, `status`, `project`, `organization`, `volunteer`, `reviewable`, `reason`, `certificationStatus` (`Pending`, `Certified`), `endDate`.

**OrganizationProfileDto**: `organizationContact` (`contactFirstname`, `contactMiddleName`, `contactLastname`, `phoneNumber`, `email`, `contactPersonProfileUrl`, `emailEditable`, `emailVerified`) and `organization` (`OrganizationDto`).

**OrganizationDto**: `organizationId`, `email`, `phoneNumber`, `name`, `numOfActiveProjects`, `category[]`, `location`, `profileCompleted`, `cacRegNumber`, `profileUrl`, `status`, `description`, `website`, `address`, `reviewStatus`.

**OrganizationDashboard**: `name`, `projects` (map of status to `ProjectResponseDto[]`), `rating`, `applicationStats` (`numApplied`, `numApproved`, `numRejected`), `status`.

**VolunteerDashboard**: `firstname`, `profileCompleted`, `projectApplications[]` (`status`, `appliedAt`).

**VolunteerApplicationDto**: `id`, `firstname`, `lastname`, `skills[]`, `reason`, `isAvailable`, `specialSkills[]`, `aboutVolunteer`, `additionalInfo`, `profileUrl`, `projectApplied` (`id`, `volunteer`, `status`, `title`, `appliedAt`). `id` is now populated with the application's ID.

**DonationDto**: `id`, `itemName`, `donor` (`VolunteerDto`, the volunteer who made the donation), `initiative`, `status`, `quantityDonated`.

**GivrMessage**: `msgId`, `content`, `projectId`, `sentBy`, `username`, `role`, `sentAt`, `savedAt`.

**MediaUploadResponse**: `signature`, `timestamp`, `expiresAt`, `apiKey`, `cloudName`, `folder`. Use these to upload media directly to the cloud storage provider, then pass the resulting URL in fields such as `profileUrl` or `projectFlierUrl`.

**VerificationSessionDto**: `id`, `name`, `submitted` (`Claims`: `organizationName`, `address`, `cacRegNumber`, `idNumber`, `idType`, `contactDateOfBirth`, `contactFullName`, `verificationStatus`, `remark`), `cacDocumentImageUrl`, `reviewStatus`, `reviewNote`.

**VolunteerVerificationSessionDto**: `verificationSessionId`, `idType`, `idNumber`, `firstname`, `middleName`, `lastname`, `individualLogo`, `dateOfBirth`, `verificationStatus`. `remark` is declared but not populated yet.

**ReviewResponseDto**: `reviewId`, `review`, `isApproved`, `reviewedBy` (`Admin`: `adminId`, `email`, `isAdmin`, `role`), `status`.

**VolunteerCertificateVerificationDTO**: `certificate`, `volunteerFirstName`, `volunteerMiddleName`, `volunteerLastName`.

**CheckoutResponse**: `checkoutUrl`. **EmailExists**: `email`, `exists`. **AuthDetailsDto**: `userId`, `email`.

### Enums

| Enum | Values |
| --- | --- |
| Project/initiative `status` | `DRAFT`, `OPEN`, `CLOSE`, `ONGOING`, `COMPLETED`, `ALL` |
| Verification `status` | `VERIFIED`, `UNVERIFIED`, `AUTOMATIC_VERIFICATION_FAILED`, `AUTOMATIC_VERIFICATION_SUCCEEDED`, `AUTOMATIC_VERIFICATION_PENDING`, `PENDING` |
| Review status | `Approved`, `Rejected`, `Pending` |
| Participation `status` | `IN_PROGRESS`, `COMPLETED`, `REJECTED` |
| Application `status` | `APPLIED`, `APPROVED`, `REJECTED` |
| Donation `status` | `Pending_acceptance`, `Promised_Accepted`, `Promise_Fulfilled`, `Promise_Rejected` |
| Certification status | `Pending`, `Certified` |
| `role` | `ORGANIZATION`, `VOLUNTEER`, `ADMIN` |
| Admin `role` | `SUPER_ADMIN`, `ADMIN` |
| `idType` | `DL`, `NIN` (only these two are accepted) |
| `applicationType` | `Items_Only`, `Items_and_time`, `Time` |
| Resource `type` | `Organization`, `Individual` |
| Media `mediaType` | `Video` |

## 10. Typical flows

**Volunteer onboarding:** `POST /volunteer/auth/signup` → `POST /volunteer/otp/request?purpose=EMAIL_VERIFICATION` → `PATCH /volunteer/verify/email` → `POST /volunteer/profile/initiate/verification` (see section 3; include `callbackUrl` and redirect to `checkoutUrl`).

**Organization lifecycle:** signup and email OTP → `POST /organization/verification/initiate` (include `callbackUrl`) → admin reviews via `/admin/review` → `POST /organization/projects` → `PATCH .../publish` → review applicants (`accept`/`reject`) → `PATCH .../completed`.

**Certificates:** organization sets participant to `COMPLETED` (`PATCH /organization/projects/participant`) → admin lists `/admin/certificate/pending` → approves one or a batch → volunteer certificate appears on `VolunteerProfile.certificates` → anyone can check it with `GET /certificates/verify/{certId}`.

**Initiative donation:** volunteer applies with `items` → individual accepts the application → donation status moves `Pending_acceptance` → `Promised_Accepted` → `Promise_Fulfilled` (or `Promise_Rejected`) via `PATCH /individual/initiative/donations/{donationId}`.


Deployment: Flyway migration `V5__alter_project.sql` adds `project.is_deleted` (not null, backfilled to `false`). Projects are now filtered with `is_deleted=false`, so run it before deploying.

## 12. Test accounts (dev environment)

In the dev environment (`https://dev.givr.ng`), only the two accounts below can pass verification. Both use `idType` `NIN`; submit the details exactly as listed.

| # | `idType` | `idNumber` (NIN) | `firstname` | `lastname` |
| --- | --- | --- | --- | --- |
| 1 | `NIN` | 63184876213 | Bunch | Dillon |
| 2 | `NIN` | 18482561982 | Guion | Audi |