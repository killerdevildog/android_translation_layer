#!/usr/bin/env python3

import argparse
import html
import re
import sys
import xml.etree.ElementTree as ET

def categorize_testcases(xml_path):
	with open(xml_path, encoding="utf-8") as f:
		xml_text = f.read()

	# XML hates raw newlines, google doesn't care. So we need to nearly double the execution time
	# by running a regex on the whole xml
	xml_text = re.sub(r'message="[^\"]*"', lambda m: m.group().replace('\n', '&#10;'), xml_text)

	tree = ET.ElementTree(ET.fromstring(xml_text))
	root = tree.getroot()

	successful_tests = []
	failed_tests = []
	skipped_tests = []

	for testsuite in root.findall('.//testsuite'):
		for testcase in testsuite.findall('testcase'):
			name = testcase.get('name')
			classname = testcase.get('classname')

			category = None

			failure = testcase.find('failure')

			if failure is not None:
				message = failure.get('message')
				# Skipped tests get reported as failures for some reason,
				# but thankfully we can indentify them like this
				if message is not None and 'Skipped' in message:
					category = skipped_tests
				else:
					category = failed_tests
			else:
				# No failure node means successful test
				category = successful_tests

			category.append({
				'name': name,
				'classname': classname,
				'message': message if failure is not None else None,
			})

	return successful_tests, failed_tests, skipped_tests





def generate_html_report(successful, failed, skipped, output_file):
	# Group tests by classname
	def group_by_classname(tests):
		grouped = {}
		for test in tests:
			classname = test['classname']
			if classname not in grouped:
				grouped[classname] = []
			grouped[classname].append(test)
		return grouped

	# HTML template
	html_content = ['''
	<!DOCTYPE html>
	<html lang="en">
		<head>
			<meta charset="UTF-8">
			<title>Test Results Report</title>
			<style>
				body {
					font-family: Arial, sans-serif;
					margin: 0 auto;
					padding: 20px;
				}
				.test-class {
					border: 1px solid #ddd;
					margin-bottom: 20px;
					padding: 15px;
					background-color: #f9f9f9;
				}
				.test-class h2 {
					margin-top: 0;
					border-bottom: 1px solid #ddd;
					padding-bottom: 10px;
				}
				.successful {
					color: green;
				}
				.failed {
					color: red;
				}
				.skipped {
					color: orange;
				}
				.test-item {
					margin-bottom: 10px;
					padding: 5px;
					background-color: #fff;
				}
				.test-category {
					overflow: hidden;
					text-overflow: ellipsis;
				}
				.test-category>h2 {
					cursor: pointer;
				}
				.failure-message {
					background-color: #ffeeee;
					border: 1px solid #ffcccc;
					cursor: pointer;
					padding: 4px;
					margin-top: 5px;
					white-space: pre-wrap;
					overflow: hidden;
					text-overflow: ellipsis;
					font-size: 0.9em;
				}
			</style>
			<script>
				function toggleShow(element, closed_height) {
					if (element.style.height == '')
						element.style.height = closed_height;
					else
						element.style.height = '';
				}
			</script>
		</head>
	<body>
		<h1>Test Results Report</h1>''']

	# Function to add tests to HTML
	def add_tests_to_html(tests, status):
		grouped_tests = group_by_classname(tests)

		html_content.append(f'''
		<div class="test-category">
			<h2 onclick="toggleShow(this.parentElement, '5em')">{status} Tests</h2>''')


		for classname, class_tests in sorted(grouped_tests.items()):
			html_content.append(f'''
			<div class="test-class">
				<h2>{classname}</h2>
				<div class="test-group {status.lower()}">''')

			for test in class_tests:
				html_content.append(f'''
					<div class="test-item {status.lower()}">
						<strong>{html.escape(test['name'])}</strong>''');
				# Add failure message for failed or skipped tests
				if test['message']:
					html_content.append(f'''
							<div class="failure-message" onclick="toggleShow(this, '2.5em')" style="height: 2.5em">{test['message']}</div>''')
				html_content.append('''
					</div>''')

			html_content.append('''
				</div>
			</div>''')

		html_content.append('''
		</div>''')

	# Add tests to HTML
	if successful:
		add_tests_to_html(successful, 'Successful')

	if failed:
		add_tests_to_html(failed, 'Failed')

	if skipped:
		add_tests_to_html(skipped, 'Skipped')

	# Close HTML
	html_content.append('''
	</body>
</html>
''')

	# Write to file
	with open(output_file, 'w', encoding='utf-8') as f:
		f.write(''.join(html_content))

def main():
	parser = argparse.ArgumentParser(description='Parse test result XML')

	parser.add_argument('xml_file_path', help='XML file containing test results')
	parser.add_argument('-s', '--successful',
	                    action='store_true',
	                    help='Print successful tests in a format accepted by --gtest_filter')
	parser.add_argument('-f', '--failed',
	                    action='store_true',
	                    help='Print failed tests in a format accepted by --gtest_filter')
	parser.add_argument('-k', '--skipped',
	                    action='store_true',
	                    help='Print skipped tests in a format accepted by --gtest_filter')
	parser.add_argument('--html',
	                    metavar='OUTPUT_FILE',
	                    help='Generate an HTML report')

	args = parser.parse_args()

	successful, failed, skipped = categorize_testcases(args.xml_file_path)

	if args.html:
		generate_html_report(successful, failed, skipped, args.html)
		print(f"HTML report generated: {args.html}")
		return

	if args.successful or args.failed or args.skipped:
		out = ""
		if args.successful:
			for test in successful:
				print(f"{test['classname']}.{test['name']}:", end='')
		if args.failed:
			for test in failed:
				print(f"{test['classname']}.{test['name']}:", end='')
		if args.skipped:
			for test in skipped:
				print(f"{test['classname']}.{test['name']}:", end='')

	else:
		print("Successful Tests:")
		for test in successful:
			print(f"- {test['classname']}.{test['name']}")

		print("\nFailed Tests:")
		for test in failed:
			print(f"- {test['classname']}.{test['name']}")

		print("\nSkipped Tests:")
		for test in skipped:
			print(f"- {test['classname']}.{test['name']}")

if __name__ == '__main__':
	main()
