# Script to replace columnDefinition = "CLOB" with @Lob annotation
Get-ChildItem -Path "src\main\java\com\samjhadoo\_disabled" -Recurse -Filter "*.java" | ForEach-Object {
    $content = Get-Content $_.FullName -Raw
    if ($content -match 'columnDefinition\s*=\s*"CLOB"') {
        Write-Host "Fixing file: $($_.Name)"
        # Remove columnDefinition = "CLOB" from @Column annotations
        $newContent = $content -replace ',\s*columnDefinition\s*=\s*"CLOB"', ''
        $newContent = $newContent -replace 'columnDefinition\s*=\s*"CLOB"\s*,\s*', ''
        $newContent = $newContent -replace 'columnDefinition\s*=\s*"CLOB"', ''
        
        # For each @Column that had CLOB, add @Lob before it
        # This is a simplified approach - add @Lob before any @Column that comes after we removed CLOB
        Set-Content -Path $_.FullName -Value $newContent -NoNewline
    }
}
Write-Host "Done! Please manually add @Lob annotation before @Column where CLOB was removed."
