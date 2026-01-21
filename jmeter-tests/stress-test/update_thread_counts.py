#!/usr/bin/env python3
"""
Update thread counts in JMeter test plan XML file.
"""

import xml.etree.ElementTree as ET
import sys

def update_thread_counts(xml_file, view_users, manage_users, bets_users, ext_users, ramp_up, duration):
    """Update thread counts and timing in JMeter test plan"""
    tree = ET.parse(xml_file)
    root = tree.getroot()
    
    thread_groups = {
        'View Patients Thread Group': view_users,
        'Manage Patients Thread Group': manage_users,
        'Bets Thread Group': bets_users,
        'External APIs Thread Group': ext_users,
    }
    
    # Update thread counts
    for thread_group in root.findall('.//ThreadGroup'):
        name = thread_group.get('testname', '')
        if name in thread_groups:
            # Update num_threads
            num_threads_elem = thread_group.find('intProp[@name="ThreadGroup.num_threads"]')
            if num_threads_elem is not None:
                num_threads_elem.text = str(thread_groups[name])
        
        # Update ramp_time and duration for ALL thread groups
        ramp_time_elem = thread_group.find('intProp[@name="ThreadGroup.ramp_time"]')
        if ramp_time_elem is not None:
            ramp_time_elem.text = str(ramp_up)
        
        duration_elem = thread_group.find('longProp[@name="ThreadGroup.duration"]')
        if duration_elem is not None:
            duration_elem.text = str(duration)
    
    # Write back
    tree.write(xml_file, encoding='UTF-8', xml_declaration=True)
    return True

if __name__ == '__main__':
    if len(sys.argv) < 7:
        print("Usage: python3 update_thread_counts.py <xml_file> <view> <manage> <bets> <ext> <ramp_up> <duration>")
        sys.exit(1)
    
    xml_file = sys.argv[1]
    view_users = int(sys.argv[2])
    manage_users = int(sys.argv[3])
    bets_users = int(sys.argv[4])
    ext_users = int(sys.argv[5])
    ramp_up = int(sys.argv[6])
    duration = int(sys.argv[7])
    
    update_thread_counts(xml_file, view_users, manage_users, bets_users, ext_users, ramp_up, duration)
    print(f"Updated {xml_file}")

