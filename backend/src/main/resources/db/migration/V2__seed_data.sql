-- ResolveX V2 Seed Data
-- Demo Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ADMIN', 'System Administrator with full access to all settings and records'),
(2, 'MANAGER', 'Department or Team Manager managing workloads and assignments'),
(3, 'EMPLOYEE', 'Support Agent / Technical Specialist working on assigned tickets'),
(4, 'CUSTOMER', 'End-user / Requester submitting tickets and viewing their status')
ON CONFLICT (id) DO NOTHING;

-- Demo Departments
INSERT INTO departments (id, name, description) VALUES
(1, 'Information Technology', 'Core infrastructure, corporate devices, network, and security'),
(2, 'Operations & Support', 'Customer tier-2 support, incident triage, and service desk operations'),
(3, 'Engineering & DevOps', 'Application software bug triage, cloud services, and deployments'),
(4, 'Human Resources', 'Internal employee equipment, onboarding, and workspace management')
ON CONFLICT (id) DO NOTHING;

-- Demo Teams
INSERT INTO teams (id, name, department_id, description) VALUES
(1, 'Infrastructure & Cloud', 1, 'Server infrastructure, cloud platforms, and data pipelines'),
(2, 'Workplace Tech & Helpdesk', 1, 'Laptops, internal network, VPN, and office peripherals'),
(3, 'Incident Response & Ops', 2, 'High-priority incident response and customer operational queries'),
(4, 'Application Support', 3, 'Core web application bugs, database integrity, and API issues')
ON CONFLICT (id) DO NOTHING;

-- BCrypt password hash for: Password@123
-- $2a$10$1Y8c24bHq1y4ZqW2B2zHau2F7.mYn4hK2n1M3bH5K7j8L9o0p1q2r (Standard Spring BCrypt for Password@123)
INSERT INTO users (id, username, email, password_hash, full_name, role_id, department_id, phone, active) VALUES
(1, 'admin', 'admin@resolvex.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'Alexander Wright (Admin)', 1, 1, '+1-555-0100', TRUE),
(2, 'manager.it', 'manager.it@resolvex.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'Sarah Jenkins (IT Manager)', 2, 1, '+1-555-0101', TRUE),
(3, 'manager.ops', 'manager.ops@resolvex.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'David Ross (Ops Manager)', 2, 2, '+1-555-0102', TRUE),
(4, 'tech.john', 'tech.john@resolvex.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'John Davis', 3, 1, '+1-555-0103', TRUE),
(5, 'tech.sarah', 'tech.sarah@resolvex.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'Sarah Miller', 3, 1, '+1-555-0104', TRUE),
(6, 'tech.alex', 'tech.alex@resolvex.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'Alex Turner', 3, 2, '+1-555-0105', TRUE),
(7, 'tech.emma', 'tech.emma@resolvex.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'Emma Watson', 3, 2, '+1-555-0106', TRUE),
(8, 'tech.michael', 'tech.michael@resolvex.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'Michael Chang', 3, 3, '+1-555-0107', TRUE),
(9, 'customer.alice', 'customer.alice@acme.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'Alice Sterling (Acme Corp)', 4, NULL, '+1-555-0108', TRUE),
(10, 'customer.bob', 'customer.bob@globex.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'Bob Henderson (Globex)', 4, NULL, '+1-555-0109', TRUE),
(11, 'customer.charlie', 'customer.charlie@initech.com', '$2a$10$K8e7YfR4yKjPvZqW8qY8U.3K6YkK6E5H6L7M8N9O0P1Q2R3S4T5U6', 'Charlie Vance (Initech)', 4, NULL, '+1-555-0110', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Team Members
INSERT INTO team_members (team_id, user_id, is_team_lead) VALUES
(1, 4, FALSE),
(1, 5, TRUE),
(2, 4, FALSE),
(2, 5, FALSE),
(3, 6, TRUE),
(3, 7, FALSE),
(4, 8, TRUE)
ON CONFLICT (team_id, user_id) DO NOTHING;

-- Categories
INSERT INTO categories (id, name, description, department_id, default_priority, active) VALUES
(1, 'Hardware & Peripherals', 'Physical devices, monitors, docking stations, and peripherals', 1, 'MEDIUM', TRUE),
(2, 'Network & Remote Access', 'VPN, office Wi-Fi, DNS, and connectivity issues', 1, 'HIGH', TRUE),
(3, 'Cloud Infrastructure', 'AWS/GCP resources, databases, container clusters, and VPCs', 1, 'CRITICAL', TRUE),
(4, 'Software Applications', 'Internal SaaS, productivity tools, and license activations', 2, 'MEDIUM', TRUE),
(5, 'Account & Security Access', 'Password resets, role upgrades, 2FA, and SSO login', 2, 'HIGH', TRUE),
(6, 'Billing & Invoicing', 'Enterprise subscription billing, invoice queries, and payments', 2, 'LOW', TRUE),
(7, 'Bug & Technical Defect', 'Reported software bugs, API exceptions, and regressions', 3, 'HIGH', TRUE)
ON CONFLICT (id) DO NOTHING;

-- SLA Policies
INSERT INTO sla_policies (id, name, priority, max_response_hours, max_resolution_hours, escalation_threshold_hours, is_active) VALUES
(1, 'Critical Outage SLA', 'CRITICAL', 1, 4, 3, TRUE),
(2, 'High Priority Incident SLA', 'HIGH', 2, 8, 6, TRUE),
(3, 'Standard Business SLA', 'MEDIUM', 4, 24, 20, TRUE),
(4, 'Low Priority Request SLA', 'LOW', 8, 48, 40, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Sample Tickets (20 realistic tickets)
INSERT INTO tickets (id, ticket_code, title, description, category_id, priority, status, created_by_id, assigned_to_id, team_id, due_date, created_at) VALUES
(1, 'TICK-1001', 'Production API gateway latency spike above 2000ms', 'Multiple microservices reporting timeout on auth routes. Urgent investigation needed.', 3, 'CRITICAL', 'IN_PROGRESS', 9, 8, 4, CURRENT_TIMESTAMP + INTERVAL '3 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
(2, 'TICK-1002', 'VPN connection drops intermittently for London remote staff', 'Employees in London branch report repeated disconnection every 10-15 minutes.', 2, 'HIGH', 'ASSIGNED', 10, 5, 2, CURRENT_TIMESTAMP + INTERVAL '6 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
(3, 'TICK-1003', 'Request for external 4K monitor and USB-C hub', 'New hire needs workstation display setup at desk 304 in HQ building.', 1, 'LOW', 'OPEN', 9, NULL, 2, CURRENT_TIMESTAMP + INTERVAL '40 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
(4, 'TICK-1004', 'SSO Login fails with SAML response signature error', 'Unable to log into customer portal using Okta identity provider.', 5, 'HIGH', 'IN_PROGRESS', 11, 4, 1, CURRENT_TIMESTAMP + INTERVAL '5 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
(5, 'TICK-1005', 'Database replica lag exceeding 10 minutes', 'Read-replica on US-East-1 is falling behind primary RDS instance.', 3, 'CRITICAL', 'OPEN', 10, NULL, 1, CURRENT_TIMESTAMP + INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '30 minutes'),
(6, 'TICK-1006', 'Monthly billing invoice reflects incorrect seat count', 'Subscription billed for 45 seats instead of contracted 30 seats.', 6, 'MEDIUM', 'ASSIGNED', 9, 6, 3, CURRENT_TIMESTAMP + INTERVAL '18 hours', CURRENT_TIMESTAMP - INTERVAL '5 hours'),
(7, 'TICK-1007', 'Slack integration webhook not triggering alerts', 'Automated workflow channel notifications stopped posting since yesterday.', 4, 'MEDIUM', 'ON_HOLD', 11, 7, 3, CURRENT_TIMESTAMP + INTERVAL '22 hours', CURRENT_TIMESTAMP - INTERVAL '8 hours'),
(8, 'TICK-1008', 'MacBook battery swelling and trackpad unresponsive', 'Urgent hardware safety issue reported by remote sales representative.', 1, 'HIGH', 'ASSIGNED', 10, 4, 2, CURRENT_TIMESTAMP + INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
(9, 'TICK-1009', 'Data export CSV formatting corrupted on special characters', 'Exporting audit records with non-ASCII names produces garbled encoding.', 7, 'MEDIUM', 'IN_PROGRESS', 9, 8, 4, CURRENT_TIMESTAMP + INTERVAL '16 hours', CURRENT_TIMESTAMP - INTERVAL '6 hours'),
(10, 'TICK-1010', 'DNS propagation failure for new customer domain', 'Custom vanity CNAME not resolving to cloud load balancer after 48h.', 2, 'HIGH', 'RESOLVED', 11, 5, 1, CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '12 hours'),
(11, 'TICK-1011', 'Request permission upgrade to Production AWS CloudWatch', 'Need read-only access to prod metric dashboards for Q4 reliability review.', 5, 'LOW', 'CLOSED', 10, 4, 1, CURRENT_TIMESTAMP - INTERVAL '24 hours', CURRENT_TIMESTAMP - INTERVAL '3 days'),
(12, 'TICK-1012', 'Kafka consumer group rebalancing loop in ingest cluster', 'Consumer lag is piling up due to continuous partition rebalances.', 3, 'CRITICAL', 'IN_PROGRESS', 9, 8, 4, CURRENT_TIMESTAMP + INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
(13, 'TICK-1013', 'WiFi access point AP-04 dead in conference room B', 'No signal broadcast in executive boardroom during scheduled board call.', 2, 'HIGH', 'OPEN', 11, NULL, 2, CURRENT_TIMESTAMP + INTERVAL '7 hours', CURRENT_TIMESTAMP - INTERVAL '1 hour'),
(14, 'TICK-1014', 'Reset password link email delayed by 45 minutes', 'Transactional SendGrid queue appears backlogged for outbound security tokens.', 5, 'MEDIUM', 'ASSIGNED', 10, 6, 3, CURRENT_TIMESTAMP + INTERVAL '14 hours', CURRENT_TIMESTAMP - INTERVAL '7 hours'),
(15, 'TICK-1015', 'Docker container exit code 137 OOM on batch worker', 'Nightly PDF report generator ran out of memory processing Q3 statements.', 3, 'HIGH', 'RESOLVED', 9, 8, 4, CURRENT_TIMESTAMP - INTERVAL '4 hours', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(16, 'TICK-1016', 'Keyboard replacement request for finance workstation', 'Spill damage on mechanical keyboard in accounting pod.', 1, 'LOW', 'OPEN', 11, NULL, 2, CURRENT_TIMESTAMP + INTERVAL '44 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours'),
(17, 'TICK-1017', 'API response rate limiting headers missing in v2 endpoints', 'Documentation says X-RateLimit-Remaining should be present but returns null.', 7, 'MEDIUM', 'ASSIGNED', 10, 7, 4, CURRENT_TIMESTAMP + INTERVAL '20 hours', CURRENT_TIMESTAMP - INTERVAL '4 hours'),
(18, 'TICK-1018', 'Payment gateway webhook TLS certificate expiring soon', 'Webhook endpoint SSL cert will expire in 4 days. Needs automated renewal check.', 3, 'HIGH', 'IN_PROGRESS', 9, 5, 1, CURRENT_TIMESTAMP + INTERVAL '5 hours', CURRENT_TIMESTAMP - INTERVAL '3 hours'),
(19, 'TICK-1019', 'Upgrade Jira and Confluence plugin integrations', 'Need new connector to sync ticket comments bidirectionally.', 4, 'LOW', 'OPEN', 11, NULL, 3, CURRENT_TIMESTAMP + INTERVAL '42 hours', CURRENT_TIMESTAMP - INTERVAL '5 hours'),
(20, 'TICK-1020', 'Customer portal session expires too quickly (under 5 mins)', 'Customers complaining about having to re-authenticate multiple times.', 4, 'MEDIUM', 'REOPENED', 10, 6, 3, CURRENT_TIMESTAMP + INTERVAL '12 hours', CURRENT_TIMESTAMP - INTERVAL '9 hours')
ON CONFLICT (id) DO NOTHING;

-- Ticket Comments
INSERT INTO ticket_comments (ticket_id, user_id, comment_text, is_internal) VALUES
(1, 8, 'Inspecting load balancer logs. Latency appears isolated to auth container cluster.', TRUE),
(1, 2, 'Notified customer that engineering is actively diagnosing.', FALSE),
(2, 5, 'Checking firewall routing logs on London gateway router.', TRUE),
(4, 4, 'Identity certificate thumbprint expired on the customer metadata endpoint.', TRUE),
(4, 9, 'Thanks, we are updating the metadata cert on our Okta tenant now.', FALSE),
(10, 5, 'CNAME records updated in Cloudflare DNS. Resolved.', FALSE)
ON CONFLICT DO NOTHING;

-- Ticket History
INSERT INTO ticket_history (ticket_id, changed_by_id, action, field_name, old_value, new_value) VALUES
(1, 2, 'ASSIGN', 'assigned_to_id', NULL, 'Michael Chang'),
(1, 8, 'STATUS_CHANGE', 'status', 'ASSIGNED', 'IN_PROGRESS'),
(2, 2, 'ASSIGN', 'assigned_to_id', NULL, 'Sarah Miller'),
(10, 5, 'STATUS_CHANGE', 'status', 'IN_PROGRESS', 'RESOLVED')
ON CONFLICT DO NOTHING;

-- Knowledge Base Articles
INSERT INTO knowledge_articles (id, title, content, summary, category_id, author_id, is_published, views_count, tags) VALUES
(1, 'How to configure Corporate VPN on macOS and Windows', 'Step-by-step guide to installing the WireGuard / OpenVPN client, importing profile certs, and troubleshooting connection drops.', 'Guide for installing and configuring corporate remote access VPN.', 2, 2, TRUE, 142, 'vpn,network,remote,helpdesk'),
(2, 'Configuring Okta Single Sign-On (SSO) with ResolveX', 'Enterprise identity integration instructions covering SAML 2.0 metadata, attribute mapping, and certificate renewal.', 'Instructions for setting up SAML 2.0 SSO via Okta.', 5, 2, TRUE, 89, 'sso,okta,security,login'),
(3, 'Resolving Out of Memory (OOM) Errors in Docker Workloads', 'Best practices for setting memory limits, JVM heap flags (-XX:MaxRAMPercentage), and diagnosing container termination code 137.', 'Diagnosis and mitigation strategies for container OOM exits.', 3, 8, TRUE, 65, 'docker,memory,cloud,devops'),
(4, 'Requesting New Hardware Peripherals and Monitors', 'Policies for hardware refreshes, approved ergonomic equipment, and standard delivery timelines.', 'Company policy and procedure for ordering peripherals.', 1, 3, TRUE, 210, 'hardware,monitor,procurement')
ON CONFLICT (id) DO NOTHING;

-- Notifications
INSERT INTO notifications (user_id, title, message, notification_type, is_read, reference_id, reference_type) VALUES
(8, 'New High Priority Ticket Assigned', 'You have been assigned to TICK-1001: Production API gateway latency spike', 'TICKET_ASSIGNED', FALSE, 1, 'TICKET'),
(5, 'Ticket Assigned', 'You have been assigned to TICK-1002: VPN connection drops intermittently', 'TICKET_ASSIGNED', FALSE, 2, 'TICKET'),
(2, 'Critical Ticket Unassigned', 'TICK-1005: Database replica lag requires immediate assignment', 'SLA_WARNING', FALSE, 5, 'TICKET')
ON CONFLICT DO NOTHING;

-- Audit Logs
INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, change_summary, ip_address) VALUES
(1, 'admin', 'SYSTEM_INITIALIZATION', 'SYSTEM', 1, 'System initialized with baseline schema and seed configuration', '127.0.0.1'),
(1, 'admin', 'POLICY_UPDATE', 'SLA_POLICY', 1, 'Critical Outage SLA configured to 4h resolution', '127.0.0.1'),
(2, 'manager.it', 'TICKET_ASSIGNMENT', 'TICKET', 1, 'Assigned ticket TICK-1001 to tech Michael Chang', '192.168.1.10')
ON CONFLICT DO NOTHING;
