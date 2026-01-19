#!/usr/bin/env python3
"""
Generate JMeter stress test plan with step-wise load increase.
Load levels: 50, 75, 100, 125, 150, 200 users
Each step: ramp-up 3 minutes, hold 10 minutes
"""

import xml.etree.ElementTree as ET
import sys
import os

# Load distribution: total_users -> (view, manage, bets, external)
LOAD_DISTRIBUTIONS = {
    50: (20, 15, 10, 5),
    75: (30, 22, 15, 8),
    100: (40, 30, 20, 10),
    125: (50, 37, 25, 13),
    150: (60, 45, 30, 15),
    200: (80, 60, 40, 20),
}

RAMP_UP = 180  # 3 minutes
HOLD_TIME = 600  # 10 minutes
DURATION = RAMP_UP + HOLD_TIME  # 13 minutes

def create_thread_group(root, name, num_threads, ramp_up, duration, start_time=0):
    """Create a ThreadGroup element"""
    thread_group = ET.SubElement(root, 'ThreadGroup')
    thread_group.set('guiclass', 'ThreadGroupGui')
    thread_group.set('testclass', 'ThreadGroup')
    thread_group.set('testname', name)
    thread_group.set('enabled', 'true')
    
    # Number of threads
    num_threads_elem = ET.SubElement(thread_group, 'intProp')
    num_threads_elem.set('name', 'ThreadGroup.num_threads')
    num_threads_elem.text = str(num_threads)
    
    # Ramp-up time
    ramp_time_elem = ET.SubElement(thread_group, 'intProp')
    ramp_time_elem.set('name', 'ThreadGroup.ramp_time')
    ramp_time_elem.text = str(ramp_up)
    
    # Duration
    duration_elem = ET.SubElement(thread_group, 'longProp')
    duration_elem.set('name', 'ThreadGroup.duration')
    duration_elem.text = str(duration)
    
    # Other properties
    same_user_elem = ET.SubElement(thread_group, 'boolProp')
    same_user_elem.set('name', 'ThreadGroup.same_user_on_next_iteration')
    same_user_elem.text = 'true'
    
    scheduler_elem = ET.SubElement(thread_group, 'boolProp')
    scheduler_elem.set('name', 'ThreadGroup.scheduler')
    scheduler_elem.text = 'true'
    
    error_elem = ET.SubElement(thread_group, 'stringProp')
    error_elem.set('name', 'ThreadGroup.on_sample_error')
    error_elem.text = 'continue'
    
    # Loop controller
    loop_controller = ET.SubElement(thread_group, 'elementProp')
    loop_controller.set('name', 'ThreadGroup.main_controller')
    loop_controller.set('elementType', 'LoopController')
    loop_controller.set('guiclass', 'LoopControlPanel')
    loop_controller.set('testclass', 'LoopController')
    loop_controller.set('testname', 'Loop Controller')
    
    loops_elem = ET.SubElement(loop_controller, 'intProp')
    loops_elem.set('name', 'LoopController.loops')
    loops_elem.text = '-1'
    
    continue_forever_elem = ET.SubElement(loop_controller, 'boolProp')
    continue_forever_elem.set('name', 'LoopController.continue_forever')
    continue_forever_elem.text = 'false'
    
    return thread_group

def copy_http_requests_from_base(base_tree, target_hash_tree, thread_group_name):
    """Copy HTTP requests from base test plan to target thread group"""
    # Find the thread group in base plan
    base_root = base_tree.getroot()
    base_hash_tree = base_root.find('.//hashTree')
    
    # Find thread groups in base
    for thread_group in base_root.findall('.//ThreadGroup'):
        if thread_group.get('testname') == thread_group_name:
            # Find its hashTree
            parent = None
            for elem in base_root.iter():
                if elem == thread_group:
                    parent = elem
                    break
            
            # Get the next hashTree after this thread group
            found_thread_group = False
            for elem in base_hash_tree.iter():
                if found_thread_group:
                    if elem.tag == 'hashTree':
                        # Copy all children
                        for child in elem:
                            target_hash_tree.append(child)
                        return
                if elem == thread_group:
                    found_thread_group = True

def generate_stress_test_plan(base_plan_path, output_path):
    """Generate stress test plan with step-wise load increase"""
    
    # Parse base test plan
    base_tree = ET.parse(base_plan_path)
    base_root = base_tree.getroot()
    
    # Create new test plan
    test_plan = ET.Element('jmeterTestPlan')
    test_plan.set('version', '1.2')
    test_plan.set('properties', '5.0')
    test_plan.set('jmeter', '5.6.3')
    
    hash_tree = ET.SubElement(test_plan, 'hashTree')
    
    # Copy TestPlan element from base
    base_test_plan = base_root.find('.//TestPlan')
    if base_test_plan is not None:
        hash_tree.append(base_test_plan)
    
    # Copy configuration elements (AuthManager, CookieManager, HTTP Request Defaults)
    for config_elem in ['AuthManager', 'CookieManager', 'ConfigTestElement']:
        elem = base_root.find(f'.//{config_elem}')
        if elem is not None:
            hash_tree.append(elem)
            # Add empty hashTree after config element
            config_hash_tree = ET.SubElement(hash_tree, 'hashTree')
    
    # Create thread groups for each load level
    step_start_time = 0
    step_duration = DURATION
    
    for total_users in [50, 75, 100, 125, 150, 200]:
        view, manage, bets, ext = LOAD_DISTRIBUTIONS[total_users]
        
        # View Patients Thread Group
        tg_view = create_thread_group(
            hash_tree, 
            f'View Patients - {total_users} users',
            view, RAMP_UP, DURATION
        )
        tg_view_hash = ET.SubElement(hash_tree, 'hashTree')
        # TODO: Copy HTTP requests from base
        
        # Manage Patients Thread Group
        tg_manage = create_thread_group(
            hash_tree,
            f'Manage Patients - {total_users} users',
            manage, RAMP_UP, DURATION
        )
        tg_manage_hash = ET.SubElement(hash_tree, 'hashTree')
        
        # Bets Thread Group
        tg_bets = create_thread_group(
            hash_tree,
            f'Bets - {total_users} users',
            bets, RAMP_UP, DURATION
        )
        tg_bets_hash = ET.SubElement(hash_tree, 'hashTree')
        
        # External APIs Thread Group
        tg_ext = create_thread_group(
            hash_tree,
            f'External APIs - {total_users} users',
            ext, RAMP_UP, DURATION
        )
        tg_ext_hash = ET.SubElement(hash_tree, 'hashTree')
        
        step_start_time += step_duration + 60  # Add 1 minute delay between steps
    
    # Add listeners
    aggregate_report = ET.SubElement(hash_tree, 'ResultCollector')
    aggregate_report.set('guiclass', 'StatVisualizer')
    aggregate_report.set('testclass', 'ResultCollector')
    aggregate_report.set('testname', 'Aggregate Report')
    aggregate_report.set('enabled', 'true')
    
    # Write output
    tree = ET.ElementTree(test_plan)
    ET.indent(tree, space='  ')
    tree.write(output_path, encoding='UTF-8', xml_declaration=True)
    
    print(f"Stress test plan generated: {output_path}")
    print(f"Load levels: 50, 75, 100, 125, 150, 200 users")
    print(f"Each step: ramp-up {RAMP_UP}s, hold {HOLD_TIME}s, total {DURATION}s")

if __name__ == '__main__':
    if len(sys.argv) < 3:
        print("Usage: python3 generate_stress_test.py <base_plan.jmx> <output_plan.jmx>")
        sys.exit(1)
    
    base_plan = sys.argv[1]
    output_plan = sys.argv[2]
    
    if not os.path.exists(base_plan):
        print(f"Error: Base test plan not found: {base_plan}")
        sys.exit(1)
    
    generate_stress_test_plan(base_plan, output_plan)

