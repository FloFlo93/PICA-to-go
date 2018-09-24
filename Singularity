Bootstrap: yum
OSVersion: 7
MirrorURL: http://mirror.centos.org/centos-%{OSVERSION}/%{OSVERSION}/os/$basearch/
Include: yum

# curl (to install Java 10)
%appinstall curl
yum install curl
 
# java-jre10 
%appinstall java-jre10
curl -L -b "oraclelicense=a" -O http://download.oracle.com/otn-pub/java/jdk/10.0.2+13/19aef61b38124481863b1413dce1855f/jre-10.0.2_linux-x64_bin.rpm
yum -y localinstall jre-10.0.2_linux-x64_bin.rpm

# tar
%appinstall tar
yum -y install tar

# xz
%appinstall xz
yum -y install xz

# pica-to-go
%appinstall pica-to-go
curl -LO https://github.com/FloFlo93/PICA-to-go/releases/download/1.0/PICA-to-go-1.0.tar.xz
tar -xf PICA-to-go-1.0.tar.xz
rm -rf PICA-to-go-1.0.tar.xz
mkdir /opt/pica-to-go/
mv bin /opt/pica-to-go/

# numpy
%appinstall numpy
yum -y install numpy

# runscript
%runscript 
/opt/pica-to-go/bin/pica-to-go $@
