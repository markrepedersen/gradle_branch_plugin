# Exercise Instructions

:wave: Hello! Thanks so much for participating in our interview process. We know that it takes time and effort away from other things and we appreciate it.

This technical exercise is our primary means of understanding your programming abilities and the beginning of our understanding of your qualifications. Build something you are proud of! 

We expect this will take about two hours but there is no time limit, no penalty for taking longer.

### Scenario 

Every Friday the Release Manager does a code freeze in preparation for the next release. Currently the Release Manager does all the code freeze work manually. The Release Manager's code freeze process involves: 

- Updating `release.plist` file with release name/version for the next release found in `releng/release_info.csv`.
- Performing the code freeze by cutting a new branch with from the base branch with name `release_name/release_version` e.g. `Date/1.3`. 
- Generating a Feature Flag report which is a diff between the current and the previous release of the file `featureflags/FF.csv` 

Your task is to implement a command line tool to help automate as much of our code freeze process as possible. When designing your tool, consider that it would be used by folks with less technical expertise. The tool should also be able to handle some exceptions and fail gracefully. 

Feel free to use your favorite programming language and framework (we use Python internally) but the code should be able to run on modern Mac hardware and OS. 

Plan for the tool to be extended over time, and use of the Github API is prefered.

### Implementation Details

#### Files
- All release names and their equivalent release versions are defined in the file `releng/release_info.csv`. The entries found in `releng/release_info.csv` are used to update `release.plist`. 
- Feature Flags settings are stored in the file `featureflags/FF.csv`.

#### Setup
- The current release for which code freeze is to be exectued is `Cake/1.2`. 
- The next release is `Date/1.3`. 

#### Expectations 

Your code should be able to do at least the following:
- Create a separate Git branch to represent the current release e.g. `Cake/1.2`. `Cake/1.2` is already setup in the `release.plist` file.
- Generate a feature flag report (text file is fine) to see what feature flags have changed since the last release. (E.g. what FF have changed between `Cake/1.2` and the previous release `Beer/1.1`. If the previous release branch is no longer present in source code e.g. `Beer/1.1` branch is not present in Git - simply output the contents of the `featureflags/FF.csv`).  
- After performing code freeze, make sure the release file, `release.plist` is updated to reflect the next release name and version in the base branch. (E.g. update release.plist in the base branch from `Cake/1.2` to `Date/1.3`). 

### Requirements
- Please create a dev branch `dev` out of this repo. In your dev branch, create a directory `release-tools` for your code and put all your delieverables under that directory. 
- Along with your code, include a write up of the test cases you would create in a text file in the `release-tools` directory. There is no need to create real tests.
- Once you finish, create a Pull Request against Master of this repo.

We don’t expect that you’ll create a production-ready tool in two hours, but we would like you to show us how you create clean, maintainable code. Once you're happy with the result email us that you have completed the challenge.

### Caveats
- You might run into problems with 2FA while interacting with GitHub API using a username/password. Therefore, it is advised to setup a GitHub token to overcome 2FA limitations. 

Good luck! :four_leaf_clover:
