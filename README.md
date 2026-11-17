# Prime Number Checker — Java + DevOps

A small browser-based Prime Number Checker built with Java 17. It includes Git/GitHub workflow, a Jenkins pipeline, Docker packaging, and Kubernetes Deployment + NodePort Service.

## Project files

```text
PrimeNumber-DevOps-Project/
├── PrimeNumber.java
├── index.html
├── Dockerfile
├── Jenkinsfile
├── .gitignore
├── README.md
└── kubernetes/
    └── prime-deployment.yaml
```

## 1. Run locally (Windows PowerShell)

Open PowerShell in this project folder:

```powershell
javac PrimeNumber.java
$env:PORT=8080
java PrimeNumber
```

Open `http://localhost:8080` in your browser. Enter a number and select **Check Number**.

Stop the server with `Ctrl + C`.

## 2. Git and GitHub

Create a **public** GitHub repository named `PrimeNumber-DevOps-Project` (do not initialize it with a README if you are pushing this folder).

Run these commands from the project folder, replacing the URL if your repository name differs:

```powershell
git init
git add .
git commit -m "Initial commit - Prime Number DevOps project"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/PrimeNumber-DevOps-Project.git
git push -u origin main
```

For later changes:

```powershell
git add .
git commit -m "Update Prime Number project"
git push
```

## 3. Jenkins pipeline

Prerequisites: Jenkins is running, Git is installed, JDK 17 is configured and available to the Jenkins service, and Docker Desktop is running.

1. Open `http://localhost:8080`.
2. Select **New Item**.
3. Enter `PrimeNumber-Web-Pipeline`, choose **Pipeline**, then select **OK**.
4. Under **Build Triggers**, optionally enable **GitHub hook trigger for GITScm polling**. You can also run builds manually with **Build Now**.
5. Under **Pipeline**, set **Definition** to **Pipeline script from SCM**.
6. Set **SCM** to **Git**.
7. Enter your GitHub repository URL.
8. Set **Branch Specifier** to `*/main`.
9. Set **Script Path** to `Jenkinsfile`, then **Save**.
10. Select **Build Now** and inspect **Console Output**.

The included Jenkinsfile uses the Docker executable path from the Windows environment used in the supplied lab setup:
`C:\Users\deeek\AppData\Local\Programs\DockerDesktop\resources\bin\docker.exe`

If Docker is installed elsewhere, update `DOCKER_EXE` in `Jenkinsfile`. If Jenkins cannot find `javac`, configure JDK 17 under **Manage Jenkins → Tools** and ensure the job uses that JDK.

## 4. Build and test Docker manually

From the project root:

```powershell
docker build -t prime-number-web:latest .
docker run --rm -p 8080:8080 prime-number-web:latest
```

Open `http://localhost:8080`. Stop with `Ctrl + C`.

## 5. Deploy to Kubernetes (Docker Desktop)

Make sure Kubernetes is enabled in Docker Desktop and the current context is the Docker Desktop cluster:

```powershell
kubectl config current-context
kubectl get nodes
```

Build the image locally first (or run the Jenkins pipeline), so the local Docker Desktop Kubernetes runtime can use `prime-number-web:latest`.

Apply the manifest:

```powershell
kubectl apply -f kubernetes/prime-deployment.yaml
kubectl get deployments
kubectl get pods
kubectl get services
```

Wait until the pod is `1/1 Running`, then open:

`http://localhost:30082`

The NodePort is `30082`, chosen to avoid the common lab NodePort `30080`.

View application logs:

```powershell
kubectl logs deployment/prime-number-web
```

Update after a new image build:

```powershell
kubectl rollout restart deployment/prime-number-web
kubectl rollout status deployment/prime-number-web
```

Remove only this project's Kubernetes resources:

```powershell
kubectl delete -f kubernetes/prime-deployment.yaml
```

## 6. Troubleshooting

- **Docker command not found in Jenkins:** update the `DOCKER_EXE` value in `Jenkinsfile` to the actual `docker.exe` path available on the Jenkins machine.
- **`javac` not found:** install/configure JDK 17 for the Jenkins service and confirm `javac -version` works in the Jenkins build environment.
- **`ErrImageNeverPull`:** build the image using Docker Desktop's local engine: `docker build -t prime-number-web:latest .`
- **Pod not ready:** inspect `kubectl describe pod <pod-name>` and `kubectl logs deployment/prime-number-web`.
- **Port already in use:** change `nodePort: 30082` to an unused NodePort in the allowed range (30000–32767), then apply the manifest again.

## How it works

- The browser page (`index.html`) sends the entered number to `/api/prime`.
- The Java HTTP server (`PrimeNumber.java`) checks divisibility up to the square root of the number and returns the result.
- Jenkins checks out the GitHub repository, compiles Java, and builds the Docker image.
- Kubernetes runs the web server as a long-running Deployment and exposes it through a NodePort Service.
