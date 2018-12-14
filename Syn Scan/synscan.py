#!/usr/bin/env python2.7
#include location of pcap file in input

import dpkt, socket, sys

ratio = 1
f = open(sys.argv[1])
log = dpkt.pcap.Reader(f)
ips = dict() 
countpkts = 0    

for s, et in log:
    countpkts += 1 
    print "printing"
    eth = dpkt.ethernet.Ethernet(et)
    ip = eth.data
    tcp = ip.data
    if type(tcp) != dpkt.tcp.TCP:
        continue
    print "printing"
    ll = set() 
    if tcp.flags & dpkt.tcp.TH_SYN  != 0:
        ll.add('syn')
    Flag = ll

    sourceIP = socket.inet_ntoa(ip.src)

    print "printing"
    if {'syn'} == set(Flag):          
        if sourceIP not in ips: ips[sourceIP] = {'syn': 0}
        ips[sourceIP]['syn'] += 1
   


for x in ips.keys():
    syns = ips[x]['syn']
    print "printing"
    print "{0:15} : {1} SYN attacks".format(x, syns)