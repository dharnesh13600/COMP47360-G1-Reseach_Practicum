# 🤝 Contributing to COMP47360-G1-Reseach_Practicum

Welcome! This document outlines the conventions and workflow for contributing to our project. By following these practices, we keep our codebase clean, reviewable, and ready for deployment to Google Kubernetes Engine (GKE).
---
## 🔀 1. Branching Model

We use a **monorepo** with **long-lived team branches**:

- `main` – stable production-ready code (used for deployment)
- `frontend` – UI development branch
- `backend` – Spring Boot backend development branch
- `data-analytics` – data pipelines, analysis scripts

### 🌱 Creating a Feature Branch

1. Start all work by branching from your team branch:

```bash
git checkout frontend                # or backend, data-analytics
git pull origin frontend             # sync with latest
git checkout -b feature/123-login-ui # feature or bugfix branch
```
2. Push your branch
   ```bash
    git push -u origin feature/{describe feature name max upto 3 words}
   ```
3. Open a PR(Pull Request)
     Base Branch : your team branch(frontend, backend, data-analytics)
     Title: feat(frontend): add login UI
   
5. Review & Approval:
  Code owners will be auto-requested via .github/CODEOWNERS
  Wait for approval and/or address comments

7. Merge and Delete:
    Once approved, merge the PR into your team branch
    Then delete the feature branch
    Team Branch → Main Merge:

At the end of sprint (or daily), open a PR from your team branch to main

This integrates all tested work into production
