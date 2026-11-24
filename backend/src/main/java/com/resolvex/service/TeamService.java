package com.resolvex.service;

import com.resolvex.entity.Department;
import com.resolvex.entity.Team;
import com.resolvex.entity.TeamMember;
import com.resolvex.entity.User;
import com.resolvex.exception.ResourceNotFoundException;
import com.resolvex.repository.DepartmentRepository;
import com.resolvex.repository.TeamMemberRepository;
import com.resolvex.repository.TeamRepository;
import com.resolvex.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;

    public TeamService(TeamRepository teamRepository,
                       DepartmentRepository departmentRepository,
                       TeamMemberRepository teamMemberRepository,
                       UserRepository userRepository) {
        this.teamRepository = teamRepository;
        this.departmentRepository = departmentRepository;
        this.teamMemberRepository = teamMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllTeams() {
        return teamRepository.findAll().stream().map(this::toTeamMap).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> createTeam(String name, Long departmentId, String description) {
        Department dept = null;
        if (departmentId != null) {
            dept = departmentRepository.findById(departmentId).orElse(null);
        }
        Team team = new Team(name.trim(), dept, description);
        Team saved = teamRepository.save(team);
        return toTeamMap(saved);
    }

    @Transactional
    public void addTeamMember(Long teamId, Long userId, Boolean isLead) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", "id", teamId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        teamMemberRepository.findByTeamIdAndUserId(teamId, userId).ifPresentOrElse(
                m -> {
                    m.setIsTeamLead(Boolean.TRUE.equals(isLead));
                    teamMemberRepository.save(m);
                },
                () -> {
                    TeamMember member = new TeamMember(team, user, Boolean.TRUE.equals(isLead));
                    teamMemberRepository.save(member);
                }
        );
    }

    @Transactional
    public void removeTeamMember(Long teamId, Long userId) {
        teamMemberRepository.deleteByTeamIdAndUserId(teamId, userId);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTeamMembers(Long teamId) {
        return teamMemberRepository.findByTeamId(teamId).stream().map(tm -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", tm.getId());
            map.put("userId", tm.getUser().getId());
            map.put("username", tm.getUser().getUsername());
            map.put("fullName", tm.getUser().getFullName());
            map.put("email", tm.getUser().getEmail());
            map.put("isTeamLead", tm.getIsTeamLead());
            return map;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> toTeamMap(Team t) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", t.getId());
        map.put("name", t.getName());
        map.put("description", t.getDescription());
        map.put("departmentId", t.getDepartment() != null ? t.getDepartment().getId() : null);
        map.put("departmentName", t.getDepartment() != null ? t.getDepartment().getName() : null);
        return map;
    }
}
