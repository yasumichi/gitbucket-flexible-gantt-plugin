# GitBucket Flexible Gantt Plugin

Powered by [Frappe Gantt](https://github.com/frappe/gantt)

## Add the following to the issue side bar:
- Start Date
- End Date
- Progress
- Dependencies

When you open an issue, it is displayed in view mode.

<img width="385" height="209" alt="Flexible Gantt sidebar with view mode" src="https://github.com/user-attachments/assets/ac85fd39-50be-42d5-a849-c7372a06ad36" />

Click the pencil icon in the top right corner to enter edit mode.

<img width="385" height="239" alt="Flexible Gantt sidebar with edit mode" src="https://github.com/user-attachments/assets/160f056e-52a8-446b-8c1c-70782702b288" />

<img width="40" height="34" alt="ref issues" src="https://github.com/user-attachments/assets/aed49e29-ca48-4527-883e-fe8c6c522d6e" />Clicking the button to the right of the Dependencies text box will display a dialog for selecting an issue.

<img width="601" height="498" alt="input assistance for dependent issues" src="https://github.com/user-attachments/assets/c2ffae5f-fb97-4b39-a27d-d55d3b7813a2" />

Select the checkboxes of the dependent issues and click the Update button to set the list of dependent issues under Dependencies.

<img width="600" height="494" alt="select issues" src="https://github.com/user-attachments/assets/519f3786-6ffb-47d6-a6a2-1109129a5b79" />

<img width="385" height="239" alt="set dependencies" src="https://github.com/user-attachments/assets/56747dee-80cb-4a2b-a50d-965109b84d8a" />

Enter a search string and click the magnifying glass icon to filter your issues.

<img width="600" height="494" alt="filtering issues" src="https://github.com/user-attachments/assets/0c7436c2-fc6e-4d82-b10f-c0494f9f0344" />

If you do not have editing permissions, the pencil icon will not be displayed.

<img width="396" height="217" alt="image" src="https://github.com/user-attachments/assets/6c4d621d-8f04-469a-b018-033502bf579c" />

## Display the Gantt chart

- If you have edit permissions, start date, end date and progress rate can be changed by drag and drop.
- Clicking on a task will open the corresponding issue.
- You can filter by milestones and labels.

<img width="1920" height="945" alt="localhost_8080_gitbucket_yasumichi_ForEnglishUser_flexible-gantt" src="https://github.com/user-attachments/assets/1e5328de-fab2-46de-8ac3-1c2bfd801d89" />

## ToDo

- [ ] Write comments seriously
- [x] Properly notify users during registration
- [ ] Enable setting changes for holidays, etc.
- [x] Add input assistance for dependent issues
- [x] Enable new issue creation from the Gantt chart screen
- [x] Enable filtering by labels and milestones
