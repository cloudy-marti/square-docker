echo "============================================="
echo "=============== Install docker ==============" 
apt-get update
apt-get install apt-transport-https ca-certificates curl gnupg2 software-properties-common
curl -fsSL https://download.docker.com/linux/debian/gpg | apt-key add -
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/debian $(lsb_release -cs) stable"
apt-get update
apt-get install docker.io
systemctl start docker
usermod -aG docker ${USER}
echo "============================================="
echo "============== Install mvn ==================" 
apt-get install maven
echo "============================================="
echo "============== Install Postman ==============" 
tar -zcvf Postman-linux-x64-7.11.0.tar.gz