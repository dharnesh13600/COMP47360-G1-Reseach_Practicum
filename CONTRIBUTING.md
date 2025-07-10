# 🤝COMP47360-G1-Research_Practicum
Welcome! This document outlines the conventions and workflow for contributing to our project. By following these practices, we keep our codebase clean, reviewable and ready for deployment to Google Kubernetes Engine GKE.
## 🔀1. Branching Model
We use a **monorepo** with **long-lived team branches**:
-	`main` - stable, production-ready code (used for deployment)
-	`frontend` – UI development branch
-	`backend` – Spring Boot backend development branch
-	`data-analytics` - data pipelines and analysis scripts
## 🌱2. Creating a feature branch
1.	Start branching off from your team branch:
```bash
git checkout frontend 			# or backend, data-analytics
git pull origin frontend		# sync with the latest resource
git checkout -b feature/FE-login-UI	# use feature as prefix 
```
2.	Push your branch:
```bash
git push -u origin feature/FE-login-UI
```
## 🧷 Open a Pull Request (PR):
**Base Branch:** your team branch (frontend, backend or data-analytics)
**Title Format:**
```text
feature-frontend: add login UI
```
## ✅ Pull Request Review and Approval
Code owners are auto-requested for review via `.github/CODEOWNERS`
Ensure your code is as expected
Address feedback and wait for approval before merging
## 🔁 Merging Workflow (Feature -> Team Branch -> Main)
A.	Merging feature branch into team branch
**Once PR is approved:**
```bash
git checkout frontend                      # or backend, data-analytics
git pull origin frontend                   # make sure it’s up-to-date
git merge feature/123-login-ui             # merge the feature branch
git push origin frontend                   # push updated branch
```
B.	Merging Team Branch into main (Production)
**Base Branch:** main
**Compare:**  your team branch (frontend, backend, data-analytics)
**Title:**
```text
Merge(frontend): UI updates
```
### ✅ Merge the PR.
