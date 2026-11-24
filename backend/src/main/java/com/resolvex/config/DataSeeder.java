package com.resolvex.config;

import com.resolvex.entity.*;
import com.resolvex.enums.RoleName;
import com.resolvex.enums.TicketPriority;
import com.resolvex.enums.TicketStatus;
import com.resolvex.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SlaPolicyRepository slaPolicyRepository;
    private final TicketRepository ticketRepository;
    private final TicketCommentRepository commentRepository;
    private final KnowledgeArticleRepository articleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(RoleRepository roleRepository,
                      DepartmentRepository departmentRepository,
                      TeamRepository teamRepository,
                      UserRepository userRepository,
                      CategoryRepository categoryRepository,
                      SlaPolicyRepository slaPolicyRepository,
                      TicketRepository ticketRepository,
                      TicketCommentRepository commentRepository,
                      KnowledgeArticleRepository articleRepository,
                      PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.slaPolicyRepository = slaPolicyRepository;
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        logger.info("Verifying and seeding baseline demo data if required...");

        // Ensure roles
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.ADMIN, "System Administrator")));
        Role managerRole = roleRepository.findByName(RoleName.MANAGER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.MANAGER, "Department / Team Manager")));
        Role employeeRole = roleRepository.findByName(RoleName.EMPLOYEE)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.EMPLOYEE, "Support Specialist")));
        Role customerRole = roleRepository.findByName(RoleName.CUSTOMER)
                .orElseGet(() -> roleRepository.save(new Role(RoleName.CUSTOMER, "End-User Requester")));

        // Ensure departments
        Department itDept = departmentRepository.findByName("Information Technology")
                .orElseGet(() -> departmentRepository.save(new Department("Information Technology", "Core infrastructure and corporate devices")));
        Department opsDept = departmentRepository.findByName("Operations & Support")
                .orElseGet(() -> departmentRepository.save(new Department("Operations & Support", "Service desk and customer tier-2 support")));
        Department engDept = departmentRepository.findByName("Engineering & DevOps")
                .orElseGet(() -> departmentRepository.save(new Department("Engineering & DevOps", "Application software and cloud platforms")));

        // Ensure teams
        Team cloudTeam = teamRepository.findAll().stream().filter(t -> "Infrastructure & Cloud".equals(t.getName())).findFirst()
                .orElseGet(() -> teamRepository.save(new Team("Infrastructure & Cloud", itDept, "Server infrastructure and cloud")));
        Team helpdeskTeam = teamRepository.findAll().stream().filter(t -> "Workplace Tech & Helpdesk".equals(t.getName())).findFirst()
                .orElseGet(() -> teamRepository.save(new Team("Workplace Tech & Helpdesk", itDept, "Laptops and office tech")));
        Team opsTeam = teamRepository.findAll().stream().filter(t -> "Incident Response & Ops".equals(t.getName())).findFirst()
                .orElseGet(() -> teamRepository.save(new Team("Incident Response & Ops", opsDept, "High priority triage")));
        Team appTeam = teamRepository.findAll().stream().filter(t -> "Application Support".equals(t.getName())).findFirst()
                .orElseGet(() -> teamRepository.save(new Team("Application Support", engDept, "Core web application bug triage")));

        // Password hash for Password@123
        String hashedPass = passwordEncoder.encode("Password@123");

        // Seed Users
        User admin = seedUser("admin", "admin@resolvex.com", hashedPass, "Alexander Wright (Admin)", adminRole, itDept);
        User managerIt = seedUser("manager.it", "manager.it@resolvex.com", hashedPass, "Sarah Jenkins (IT Manager)", managerRole, itDept);
        User managerOps = seedUser("manager.ops", "manager.ops@resolvex.com", hashedPass, "David Ross (Ops Manager)", managerRole, opsDept);
        User techJohn = seedUser("tech.john", "tech.john@resolvex.com", hashedPass, "John Davis", employeeRole, itDept);
        User techSarah = seedUser("tech.sarah", "tech.sarah@resolvex.com", hashedPass, "Sarah Miller", employeeRole, itDept);
        User techAlex = seedUser("tech.alex", "tech.alex@resolvex.com", hashedPass, "Alex Turner", employeeRole, opsDept);
        User techEmma = seedUser("tech.emma", "tech.emma@resolvex.com", hashedPass, "Emma Watson", employeeRole, opsDept);
        User techMichael = seedUser("tech.michael", "tech.michael@resolvex.com", hashedPass, "Michael Chang", employeeRole, engDept);
        User customerAlice = seedUser("customer.alice", "customer.alice@acme.com", hashedPass, "Alice Sterling (Acme Corp)", customerRole, null);
        User customerBob = seedUser("customer.bob", "customer.bob@globex.com", hashedPass, "Bob Henderson (Globex)", customerRole, null);
        User customerCharlie = seedUser("customer.charlie", "customer.charlie@initech.com", hashedPass, "Charlie Vance (Initech)", customerRole, null);

        // Ensure SLA Policies
        seedSlaPolicy("Critical Outage SLA", TicketPriority.CRITICAL, 1, 4, 3);
        seedSlaPolicy("High Priority Incident SLA", TicketPriority.HIGH, 2, 8, 6);
        seedSlaPolicy("Standard Business SLA", TicketPriority.MEDIUM, 4, 24, 20);
        seedSlaPolicy("Low Priority Request SLA", TicketPriority.LOW, 8, 48, 40);

        // Ensure Categories
        Category catHardware = seedCategory("Hardware & Peripherals", "Physical devices, monitors, docking stations", itDept, TicketPriority.MEDIUM);
        Category catNetwork = seedCategory("Network & Remote Access", "VPN, office Wi-Fi, DNS, and connectivity issues", itDept, TicketPriority.HIGH);
        Category catCloud = seedCategory("Cloud Infrastructure", "AWS/GCP resources, databases, container clusters", itDept, TicketPriority.CRITICAL);
        Category catSoftware = seedCategory("Software Applications", "Internal SaaS, productivity tools, and license activations", opsDept, TicketPriority.MEDIUM);
        Category catAccess = seedCategory("Account & Security Access", "Password resets, role upgrades, 2FA, and SSO login", opsDept, TicketPriority.HIGH);
        Category catBilling = seedCategory("Billing & Invoicing", "Enterprise subscription billing, invoice queries, and payments", opsDept, TicketPriority.LOW);
        Category catBug = seedCategory("Bug & Technical Defect", "Reported software bugs, API exceptions, and regressions", engDept, TicketPriority.HIGH);

        // Ensure 20 Sample Tickets
        if (ticketRepository.count() < 10) {
            seedSampleTickets(admin, managerIt, techJohn, techSarah, techAlex, techEmma, techMichael,
                    customerAlice, customerBob, customerCharlie,
                    catHardware, catNetwork, catCloud, catSoftware, catAccess, catBilling, catBug,
                    cloudTeam, helpdeskTeam, opsTeam, appTeam);
        }

        // Ensure Knowledge Base Articles
        if (articleRepository.count() == 0) {
            articleRepository.save(new KnowledgeArticle(
                    "How to configure Corporate VPN on macOS and Windows",
                    "Detailed guide for installing WireGuard / OpenVPN profiles and testing secure connectivity.",
                    "Guide for setting up secure corporate VPN access.",
                    catNetwork, managerIt, "vpn,network,remote,helpdesk"
            ));
            articleRepository.save(new KnowledgeArticle(
                    "Configuring Okta Single Sign-On (SSO) with ResolveX",
                    "Instructions for setting up SAML 2.0 metadata, entity IDs, and user attribute assertions.",
                    "Instructions for enterprise SAML 2.0 Okta integration.",
                    catAccess, managerIt, "sso,okta,security,login"
            ));
            articleRepository.save(new KnowledgeArticle(
                    "Resolving Out of Memory (OOM) Errors in Docker Workloads",
                    "Best practices for tuning JVM memory limits and checking container termination signals.",
                    "Strategies for diagnosing container memory exhaustion.",
                    catCloud, techMichael, "docker,memory,cloud,devops"
            ));
            articleRepository.save(new KnowledgeArticle(
                    "Requesting New Hardware Peripherals and Monitors",
                    "Company policy and approval procedure for ordering ergonomic equipment and monitors.",
                    "Hardware refresh and procurement procedure.",
                    catHardware, managerOps, "hardware,monitor,procurement"
            ));
        }

        logger.info("Demo data seeding completed successfully.");
    }

    private User seedUser(String username, String email, String passwordHash, String fullName, Role role, Department dept) {
        return userRepository.findByUsername(username).map(existing -> {
            // Update password hash to guarantee match
            existing.setPasswordHash(passwordHash);
            return userRepository.save(existing);
        }).orElseGet(() -> {
            User u = new User(username, email, passwordHash, fullName, role);
            u.setDepartment(dept);
            u.setActive(true);
            return userRepository.save(u);
        });
    }

    private Category seedCategory(String name, String desc, Department dept, TicketPriority priority) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(new Category(name, desc, dept, priority)));
    }

    private void seedSlaPolicy(String name, TicketPriority priority, int response, int resolution, int escalation) {
        slaPolicyRepository.findByPriority(priority).orElseGet(() ->
                slaPolicyRepository.save(new SlaPolicy(name, priority, response, resolution, escalation)));
    }

    private void seedSampleTickets(User admin, User managerIt, User techJohn, User techSarah, User techAlex, User techEmma, User techMichael,
                                  User customerAlice, User customerBob, User customerCharlie,
                                  Category catHardware, Category catNetwork, Category catCloud, Category catSoftware,
                                  Category catAccess, Category catBilling, Category catBug,
                                  Team cloudTeam, Team helpdeskTeam, Team opsTeam, Team appTeam) {
        OffsetDateTime now = OffsetDateTime.now();

        createTicket("TICK-1001", "Production API gateway latency spike above 2000ms", "Multiple microservices reporting timeout on auth routes. Urgent investigation needed.", catCloud, TicketPriority.CRITICAL, TicketStatus.IN_PROGRESS, customerAlice, techMichael, appTeam, now.plusHours(3), now.minusHours(1));
        createTicket("TICK-1002", "VPN connection drops intermittently for London remote staff", "Employees in London branch report repeated disconnection every 10-15 minutes.", catNetwork, TicketPriority.HIGH, TicketStatus.ASSIGNED, customerBob, techSarah, helpdeskTeam, now.plusHours(6), now.minusHours(2));
        createTicket("TICK-1003", "Request for external 4K monitor and USB-C hub", "New hire needs workstation display setup at desk 304 in HQ building.", catHardware, TicketPriority.LOW, TicketStatus.OPEN, customerAlice, null, helpdeskTeam, now.plusHours(40), now.minusHours(3));
        createTicket("TICK-1004", "SSO Login fails with SAML response signature error", "Unable to log into customer portal using Okta identity provider.", catAccess, TicketPriority.HIGH, TicketStatus.IN_PROGRESS, customerCharlie, techJohn, cloudTeam, now.plusHours(5), now.minusHours(3));
        createTicket("TICK-1005", "Database replica lag exceeding 10 minutes", "Read-replica on US-East-1 is falling behind primary RDS instance.", catCloud, TicketPriority.CRITICAL, TicketStatus.OPEN, customerBob, null, cloudTeam, now.plusHours(2), now.minusMinutes(30));
        createTicket("TICK-1006", "Monthly billing invoice reflects incorrect seat count", "Subscription billed for 45 seats instead of contracted 30 seats.", catBilling, TicketPriority.MEDIUM, TicketStatus.ASSIGNED, customerAlice, techAlex, opsTeam, now.plusHours(18), now.minusHours(5));
        createTicket("TICK-1007", "Slack integration webhook not triggering alerts", "Automated workflow channel notifications stopped posting since yesterday.", catSoftware, TicketPriority.MEDIUM, TicketStatus.ON_HOLD, customerCharlie, techEmma, opsTeam, now.plusHours(22), now.minusHours(8));
        createTicket("TICK-1008", "MacBook battery swelling and trackpad unresponsive", "Urgent hardware safety issue reported by remote sales representative.", catHardware, TicketPriority.HIGH, TicketStatus.ASSIGNED, customerBob, techJohn, helpdeskTeam, now.plusHours(4), now.minusHours(4));
        createTicket("TICK-1009", "Data export CSV formatting corrupted on special characters", "Exporting audit records with non-ASCII names produces garbled encoding.", catBug, TicketPriority.MEDIUM, TicketStatus.IN_PROGRESS, customerAlice, techMichael, appTeam, now.plusHours(16), now.minusHours(6));
        createTicket("TICK-1010", "DNS propagation failure for new customer domain", "Custom vanity CNAME not resolving to cloud load balancer after 48h.", catNetwork, TicketPriority.HIGH, TicketStatus.RESOLVED, customerCharlie, techSarah, cloudTeam, now.minusHours(1), now.minusHours(12));
        createTicket("TICK-1011", "Request permission upgrade to Production AWS CloudWatch", "Need read-only access to prod metric dashboards for Q4 reliability review.", catAccess, TicketPriority.LOW, TicketStatus.CLOSED, customerBob, techJohn, cloudTeam, now.minusHours(24), now.minusDays(3));
        createTicket("TICK-1012", "Kafka consumer group rebalancing loop in ingest cluster", "Consumer lag is piling up due to continuous partition rebalances.", catCloud, TicketPriority.CRITICAL, TicketStatus.IN_PROGRESS, customerAlice, techMichael, appTeam, now.plusHours(1), now.minusHours(2));
        createTicket("TICK-1013", "WiFi access point AP-04 dead in conference room B", "No signal broadcast in executive boardroom during scheduled board call.", catNetwork, TicketPriority.HIGH, TicketStatus.OPEN, customerCharlie, null, helpdeskTeam, now.plusHours(7), now.minusHours(1));
        createTicket("TICK-1014", "Reset password link email delayed by 45 minutes", "Transactional SendGrid queue appears backlogged for outbound security tokens.", catAccess, TicketPriority.MEDIUM, TicketStatus.ASSIGNED, customerBob, techAlex, opsTeam, now.plusHours(14), now.minusHours(7));
        createTicket("TICK-1015", "Docker container exit code 137 OOM on batch worker", "Nightly PDF report generator ran out of memory processing Q3 statements.", catCloud, TicketPriority.HIGH, TicketStatus.RESOLVED, customerAlice, techMichael, appTeam, now.minusHours(4), now.minusDays(1));
        createTicket("TICK-1016", "Keyboard replacement request for finance workstation", "Spill damage on mechanical keyboard in accounting pod.", catHardware, TicketPriority.LOW, TicketStatus.OPEN, customerCharlie, null, helpdeskTeam, now.plusHours(44), now.minusHours(2));
        createTicket("TICK-1017", "API response rate limiting headers missing in v2 endpoints", "Documentation says X-RateLimit-Remaining should be present but returns null.", catBug, TicketPriority.MEDIUM, TicketStatus.ASSIGNED, customerBob, techEmma, appTeam, now.plusHours(20), now.minusHours(4));
        createTicket("TICK-1018", "Payment gateway webhook TLS certificate expiring soon", "Webhook endpoint SSL cert will expire in 4 days. Needs automated renewal check.", catCloud, TicketPriority.HIGH, TicketStatus.IN_PROGRESS, customerAlice, techSarah, cloudTeam, now.plusHours(5), now.minusHours(3));
        createTicket("TICK-1019", "Upgrade Jira and Confluence plugin integrations", "Need new connector to sync ticket comments bidirectionally.", catSoftware, TicketPriority.LOW, TicketStatus.OPEN, customerCharlie, null, opsTeam, now.plusHours(42), now.minusHours(5));
        createTicket("TICK-1020", "Customer portal session expires too quickly (under 5 mins)", "Customers complaining about having to re-authenticate multiple times.", catSoftware, TicketPriority.MEDIUM, TicketStatus.REOPENED, customerBob, techAlex, opsTeam, now.plusHours(12), now.minusHours(9));
    }

    private void createTicket(String code, String title, String desc, Category cat, TicketPriority priority, TicketStatus status,
                              User creator, User assignee, Team team, OffsetDateTime dueDate, OffsetDateTime createdAt) {
        Ticket t = new Ticket();
        t.setTicketCode(code);
        t.setTitle(title);
        t.setDescription(desc);
        t.setCategory(cat);
        t.setPriority(priority);
        t.setStatus(status);
        t.setCreatedBy(creator);
        t.setAssignedTo(assignee);
        t.setTeam(team);
        t.setDueDate(dueDate);
        t.setCreatedAt(createdAt);
        t.setUpdatedAt(createdAt);
        if (status == TicketStatus.RESOLVED) {
            t.setResolvedAt(OffsetDateTime.now());
            t.setResolutionNotes("Resolved successfully by engineer.");
        } else if (status == TicketStatus.CLOSED) {
            t.setResolvedAt(OffsetDateTime.now().minusDays(1));
            t.setClosedAt(OffsetDateTime.now().minusHours(12));
            t.setResolutionNotes("Closed and confirmed.");
        }
        Ticket saved = ticketRepository.save(t);

        // Add sample comment
        if (assignee != null) {
            commentRepository.save(new TicketComment(saved, assignee, "Investigating this ticket. Initial diagnostic underway.", false));
        }
    }
}
