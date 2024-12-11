#!/bin/bash

# Array to store unique outputs
unique_outputs=()

# Run the simulation 10 times
for ((i=1; i<=100; i++)); do
    ./sim sample2.json

    # Assuming the output is a single line in log.json
    output=$(tail -n 1 log.json)

    # Check if the output is unique
    if [[ ! " ${unique_outputs[@]} " =~ " ${output} " ]]; then
        unique_outputs+=("$output")
    fi
done

# Display the unique outputs
# echo "Unique Outputs:"
# for output in "${unique_outputs[@]}"; do
#     echo "- $output"
# done

# Display the count of unique outputs
echo "Total Unique Outputs: ${#unique_outputs[@]}"
