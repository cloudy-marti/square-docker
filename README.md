This is project Square from 2nd Year of IT engineering course in ESIPE MLV.

Authors :
    - FAU Nicolas
    - MARTI Emilie
    
The point of this project is to develop a Kubernetes-like application, used for automating application deployment, scaling and management.

oct.27 Release :
    - JSON Serialize / Deserialize functions with Quarkus
    - Hello app done with Quarkus and deployed with Docker
        - sudo dockebuild -f docker-images/Dockerfile.jvm -t quarkus/projectsquare-jvm .
        - sudo docker run -i --rm -p 8080:8080 quarkus/projectsquare-jvm
