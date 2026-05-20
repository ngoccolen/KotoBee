param(
    [Parameter(Mandatory = $true)]
    [string]$Kanjidic,

    [Parameter(Mandatory = $true)]
    [string]$KanjivgZip,

    [Parameter(Mandatory = $true)]
    [string]$Output,

    [int]$Limit = 300
)

$ErrorActionPreference = "Stop"

function ConvertTo-IntOrNull([string]$Value) {
    if ([string]::IsNullOrWhiteSpace($Value)) {
        return $null
    }

    $parsed = 0
    if ([int]::TryParse($Value.Trim(), [ref]$parsed)) {
        return $parsed
    }

    return $null
}

function Normalize-Text([string]$Value) {
    if ($null -eq $Value) {
        return ""
    }

    return $Value.Normalize([System.Text.NormalizationForm]::FormC)
}

function Get-RadicalCharacter($RadicalNumber) {
    if ($null -eq $RadicalNumber -or $RadicalNumber -lt 1 -or $RadicalNumber -gt 214) {
        return ""
    }

    return [string][char](0x2f00 + [int]$RadicalNumber - 1)
}

function Get-MappedJlptLevel($OldJlpt, $Grade) {
    switch ($OldJlpt) {
        4 { return 5 }
        3 { return 4 }
        2 { return 3 }
        1 { return 2 }
    }

    switch ($Grade) {
        1 { return 5 }
        2 { return 5 }
        3 { return 4 }
        4 { return 4 }
        5 { return 3 }
        6 { return 3 }
        8 { return 2 }
        default { return 99 }
    }
}

function Get-LevelSort($JlptLevel) {
    switch ($JlptLevel) {
        5 { return 0 }
        4 { return 1 }
        3 { return 2 }
        2 { return 3 }
        1 { return 4 }
        default { return 5 }
    }
}

function Get-NodeText($Node, [string]$TagName) {
    $children = $Node.GetElementsByTagName($TagName)
    if ($children.Count -gt 0) {
        return $children.Item(0).InnerText
    }

    return $null
}

function Get-Readings($RmGroup, [string]$Type) {
    if ($null -eq $RmGroup) {
        return @()
    }

    $values = New-Object System.Collections.Generic.List[string]
    foreach ($reading in $RmGroup.GetElementsByTagName("reading")) {
        if ($reading.GetAttribute("r_type") -eq $Type -and -not [string]::IsNullOrWhiteSpace($reading.InnerText)) {
            $values.Add($reading.InnerText.Trim())
        }
    }

    return $values.ToArray()
}

function Get-EnglishMeanings($RmGroup) {
    if ($null -eq $RmGroup) {
        return @()
    }

    $values = New-Object System.Collections.Generic.List[string]
    foreach ($meaning in $RmGroup.GetElementsByTagName("meaning")) {
        $language = $meaning.GetAttribute("m_lang")
        if ([string]::IsNullOrWhiteSpace($language) -or $language -eq "en") {
            if (-not [string]::IsNullOrWhiteSpace($meaning.InnerText)) {
                $values.Add($meaning.InnerText.Trim())
            }
        }
    }

    return $values.ToArray()
}

function Read-XmlDocument([string]$Path) {
    $stream = [System.IO.File]::OpenRead($Path)
    try {
        if ($Path.EndsWith(".gz")) {
            $gzip = New-Object System.IO.Compression.GzipStream($stream, [System.IO.Compression.CompressionMode]::Decompress)
            $reader = New-Object System.IO.StreamReader($gzip, [System.Text.Encoding]::UTF8)
        } else {
            $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
        }

        try {
            $xmlText = $reader.ReadToEnd()
        } finally {
            $reader.Dispose()
        }
    } finally {
        $stream.Dispose()
    }

    $document = New-Object System.Xml.XmlDocument
    $document.PreserveWhitespace = $false
    $document.XmlResolver = $null
    $document.LoadXml($xmlText)
    return $document
}

function Load-SvgPaths([string]$ZipPath) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $archive = [System.IO.Compression.ZipFile]::OpenRead($ZipPath)
    $pathsByCodepoint = @{}

    try {
        foreach ($entry in $archive.Entries) {
            $name = $entry.FullName.ToLowerInvariant()
            if (-not $name.EndsWith(".svg") -or -not $name.Contains("/kanji/")) {
                continue
            }

            $codepoint = [System.IO.Path]::GetFileNameWithoutExtension($name)
            $stream = $entry.Open()
            try {
                $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
                try {
                    $svgText = $reader.ReadToEnd()
                } finally {
                    $reader.Dispose()
                }
            } finally {
                $stream.Dispose()
            }

            $svg = New-Object System.Xml.XmlDocument
            $svg.XmlResolver = $null
            $svg.LoadXml($svgText)

            $strokePaths = New-Object System.Collections.Generic.List[string]
            foreach ($pathNode in $svg.GetElementsByTagName("path")) {
                $pathData = $pathNode.GetAttribute("d")
                if (-not [string]::IsNullOrWhiteSpace($pathData)) {
                    $strokePaths.Add($pathData.Trim())
                }
            }

            if ($strokePaths.Count -gt 0) {
                $pathsByCodepoint[$codepoint] = $strokePaths.ToArray()
            }
        }
    } finally {
        $archive.Dispose()
    }

    return $pathsByCodepoint
}

$svgPathsByCodepoint = Load-SvgPaths $KanjivgZip
$kanjidicDocument = Read-XmlDocument $Kanjidic
$entries = New-Object System.Collections.Generic.List[object]

foreach ($character in $kanjidicDocument.GetElementsByTagName("character")) {
    $literal = (Get-NodeText $character "literal")
    if ([string]::IsNullOrWhiteSpace($literal)) {
        continue
    }

    $codepoint = [int][char]$literal[0]
    $codepointKey = $codepoint.ToString("x5")
    if (-not $svgPathsByCodepoint.ContainsKey($codepointKey)) {
        continue
    }

    $misc = $character.GetElementsByTagName("misc").Item(0)
    $strokeCount = ConvertTo-IntOrNull (Get-NodeText $misc "stroke_count")
    $frequency = ConvertTo-IntOrNull (Get-NodeText $misc "freq")
    $oldJlpt = ConvertTo-IntOrNull (Get-NodeText $misc "jlpt")
    $grade = ConvertTo-IntOrNull (Get-NodeText $misc "grade")
    $jlptLevel = Get-MappedJlptLevel $oldJlpt $grade

    $radicalNumber = $null
    $radical = $character.GetElementsByTagName("radical").Item(0)
    if ($null -ne $radical) {
        foreach ($radValue in $radical.GetElementsByTagName("rad_value")) {
            if ($radValue.GetAttribute("rad_type") -eq "classical") {
                $radicalNumber = ConvertTo-IntOrNull $radValue.InnerText
                break
            }
        }
    }

    $rmGroup = $character.GetElementsByTagName("rmgroup").Item(0)
    $onyomi = Get-Readings $rmGroup "ja_on"
    $kunyomi = Get-Readings $rmGroup "ja_kun"
    $hanViet = Get-Readings $rmGroup "vietnam"
    $meaningEn = Get-EnglishMeanings $rmGroup
    $hanVietText = (Normalize-Text ($hanViet -join ", ")).ToUpperInvariant()
    $meaningEnText = Normalize-Text (($meaningEn | Select-Object -First 6) -join "; ")
    $displayMeaning = if (-not [string]::IsNullOrWhiteSpace($hanVietText)) { $hanVietText } else { $meaningEnText }
    $paths = $svgPathsByCodepoint[$codepointKey]

    if ($null -eq $strokeCount) {
        $strokeCount = $paths.Count
    }

    $entry = [ordered]@{
        character = $literal
        kanji = $literal
        meaning = $displayMeaning
        meaning_vi = $displayMeaning
        meaning_en = $meaningEnText
        hanViet = $hanVietText
        onyomi = $onyomi -join ([string][char]0x3001)
        kunyomi = $kunyomi -join ([string][char]0x3001)
        strokeCount = $strokeCount
        strokes = $strokeCount
        radical = Get-RadicalCharacter $radicalNumber
        radical_number = $radicalNumber
        jlptLevel = $jlptLevel
        level = if ($jlptLevel -ne 99) { "N$jlptLevel" } else { "" }
        frequency = $frequency
        sort_order = 0
        status = "published"
        svgPaths = $paths
    }

    $entries.Add([pscustomobject]$entry)
}

$sortedEntries = $entries |
    Sort-Object `
        @{ Expression = { Get-LevelSort $_.jlptLevel }; Ascending = $true },
        @{ Expression = { if ($null -eq $_.frequency) { 999999 } else { $_.frequency } }; Ascending = $true },
        @{ Expression = { $_.strokeCount }; Ascending = $true },
        @{ Expression = { $_.character }; Ascending = $true } |
    Select-Object -First $Limit

$sortOrder = 1
foreach ($entry in $sortedEntries) {
    $entry.sort_order = $sortOrder
    $sortOrder += 1
}

$json = $sortedEntries | ConvertTo-Json -Depth 20
$outputPath = [System.IO.Path]::GetFullPath($Output)
$outputDirectory = [System.IO.Path]::GetDirectoryName($outputPath)
if (-not [string]::IsNullOrWhiteSpace($outputDirectory)) {
    [System.IO.Directory]::CreateDirectory($outputDirectory) | Out-Null
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($outputPath, $json + [Environment]::NewLine, $utf8NoBom)
Write-Host "Wrote $($sortedEntries.Count) kanji entries to $outputPath"
