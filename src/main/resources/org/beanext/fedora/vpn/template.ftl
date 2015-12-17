[connection]
id=${id}
uuid=${uuid}
type=vpn
autoconnect=false
permissions=user:${sysUser}:;
secondaries=

[vpn]
NAT Traversal Mode=natt
ipsec-secret-type=save
IPSec secret-flags=0
xauth-password-type=save
Vendor=cisco
Xauth username=${username}
IPSec gateway=${host}
Xauth password-flags=0
IPSec ID=nydus
Perfect Forward Secrecy=server
IKE DH Group=dh2
Local Port=0
service-type=org.freedesktop.NetworkManager.vpnc

[vpn-secrets]
IPSec secret=n1y2d3u4s5
Xauth password=${password}

[ipv4]
dns-search=
method=auto

[ipv6]
dns-search=
method=auto
