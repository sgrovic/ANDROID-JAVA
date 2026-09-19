package com.app.cutoff.ui.project;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.app.cutoff.R;
import com.app.cutoff.data.database.entity.BudgetProjectEntity;
import com.app.cutoff.ui.common.CutoffSheetBuilder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProjectFragment extends Fragment {
    private ProjectViewModel viewModel;
    private LinearLayout container;
    private TextView empty;
    private TextView count;
    private List<BudgetProjectEntity> projects = Collections.emptyList();

    public ProjectFragment() { super(R.layout.fragment_project_list); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
        container = view.findViewById(R.id.budget_project_container);
        empty = view.findViewById(R.id.budget_project_empty);
        count = view.findViewById(R.id.budget_project_count);
        view.findViewById(R.id.button_add_project)
                .setOnClickListener(ignored -> showProjectDialog(null));
        viewModel.projects().observe(getViewLifecycleOwner(), value -> {
            projects = value == null ? Collections.emptyList() : value;
            renderProjects();
        });
    }

    private void renderProjects() {
        container.removeAllViews();
        count.setText(String.format(Locale.getDefault(), "%d active", projects.size()));
        empty.setVisibility(projects.isEmpty() ? View.VISIBLE : View.GONE);
        container.setVisibility(projects.isEmpty() ? View.GONE : View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (BudgetProjectEntity project : projects) {
            View row = inflater.inflate(R.layout.item_budget_project, container, false);
            TextView name = row.findViewById(R.id.budget_project_name);
            ImageButton more = row.findViewById(R.id.budget_project_more);
            name.setText(project.getName());
            row.setOnClickListener(ignored -> openProject(project.getId()));
            more.setOnClickListener(anchor -> showProjectMenu(anchor, project));
            container.addView(row);
        }
    }

    private void openProject(long projectId) {
        Bundle args = new Bundle();
        args.putLong(ProjectDetailFragment.ARG_PROJECT_ID, projectId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_project_to_projectDetail, args);
    }

    private void showProjectMenu(View anchor, BudgetProjectEntity project) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add(R.string.action_edit);
        menu.getMenu().add(R.string.action_delete);
        menu.setOnMenuItemClickListener(item -> {
            if (getString(R.string.action_edit).contentEquals(item.getTitle())) {
                showProjectDialog(project);
            } else {
                confirmDelete(project);
            }
            return true;
        });
        menu.show();
    }

    private void showProjectDialog(@Nullable BudgetProjectEntity project) {
        View content = getLayoutInflater().inflate(R.layout.dialog_budget_project, null);
        EditText name = content.findViewById(R.id.input_project_name);
        if (project != null) {
            name.setText(project.getName());
        }
        new CutoffSheetBuilder(requireContext())
                .setTitle(project == null ? "Add budget project" : "Edit budget project")
                .setView(content)
                .setPositiveButton(project == null ? R.string.action_add : R.string.action_save,
                        (dialog, which) -> saveProject(project, name))
                .setNegativeButton(R.string.action_cancel, null)
                .create().show();
    }

    private void saveProject(@Nullable BudgetProjectEntity project, EditText nameInput) {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) return;
        if (project == null) {
            viewModel.addProject(name);
        } else {
            project.setName(name);
            viewModel.updateProject(project);
        }
    }

    private void confirmDelete(BudgetProjectEntity project) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete project?")
                .setMessage("Delete " + project.getName() + " and all of its contributions?")
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_delete,
                        (dialog, which) -> viewModel.deleteProject(project))
                .show();
    }
}
